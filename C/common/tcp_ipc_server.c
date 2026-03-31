#include "tcp_ipc_server.h"

/*
===========================================================
  tcp_ipc_server.c

  - TCP固定のIPCサーバ基盤（共通化して流用する想定）
  - Protocol: [4byte length(big-endian)][JSON bytes]
  - epoll + non-blocking で多数接続を1プロセスで監視
  - 受信完了したリクエストはキューに積み worker が並列処理
  - 返信も同じ length-prefixed で返す

  【重要な設計意図】
  ・epollスレッド（main）は「受信完了まで」を担当
  ・重い処理（ファイルI/O/外部API/DB）は worker で担当
  ・TCPはメッセージ境界がないので length header が必須
===========================================================
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <sys/epoll.h>
#include <pthread.h>
#include <errno.h>
#include <time.h>

/* =========================================================
   ログ（複数workerからprintfされるので混ざり防止にmutex）
   ========================================================= */
static pthread_mutex_t g_log_mutex = PTHREAD_MUTEX_INITIALIZER;

static long long now_ms(void){
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    return (long long)ts.tv_sec*1000LL + ts.tv_nsec/1000000LL;
}

/* =========================================================
   [必須] ノンブロッキング化
   - epoll と組み合わせる場合 accept/read が止まらないようにする
   ========================================================= */
static void set_nonblocking(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    if (flags == -1) flags = 0;
    fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

/* workerで送信時は「確実に全部書く」ためブロッキングに戻す */
static void set_blocking(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    if (flags == -1) flags = 0;
    fcntl(fd, F_SETFL, flags & ~O_NONBLOCK);
}

/* =========================================================
   write_all（ブロッキング前提）
   - writeは部分書き込みがあり得るので必ず全量送る
   ========================================================= */
static int write_all(int fd, const void* buf, size_t len) {
    const unsigned char* p = (const unsigned char*)buf;
    size_t off = 0;
    while (off < len) {
        ssize_t n = write(fd, p + off, len - off);
        if (n < 0) {
            if (errno == EINTR) continue;
            return -1;
        }
        off += (size_t)n;
    }
    return 0;
}

/* =========================================================
   簡易JSON抽出（ログ用途のみ）
   - 本番で厳密パースが必要なら cJSON等の導入推奨
   ========================================================= */
static int json_get_string(const char* json, const char* key, char* out, size_t outsz) {
    char pat[128];
    snprintf(pat, sizeof(pat), "\"%s\"", key);
    const char* p = strstr(json, pat);
    if (!p) return 0;

    p = strchr(p, ':'); if (!p) return 0;
    p++;
    while (*p==' '||*p=='\t') p++;
    if (*p!='\"') return 0;
    p++;

    const char* q = strchr(p, '\"');
    if (!q) return 0;

    size_t n = (size_t)(q - p);
    if (n >= outsz) n = outsz - 1;
    memcpy(out, p, n);
    out[n] = '\0';
    return 1;
}

static int json_get_long(const char* json, const char* key, long* out) {
    char pat[128];
    snprintf(pat, sizeof(pat), "\"%s\"", key);
    const char* p = strstr(json, pat);
    if (!p) return 0;

    p = strchr(p, ':'); if (!p) return 0;
    p++;
    while (*p==' '||*p=='\t') p++;
    if (*p=='\"') p++;

    char* endptr = NULL;
    long v = strtol(p, &endptr, 10);
    if (endptr == p) return 0;

    *out = v;
    return 1;
}

/* =========================================================
   fdごとの受信状態（分割受信に対応するため必須）
   - hdr_read: 4byteヘッダを何byte読んだか
   - body_len/body_read: JSON本体の長さ/読み進み
   ========================================================= */
typedef struct {
    int fd;

    unsigned char hdr[4];
    int hdr_read;

    uint32_t body_len;
    uint32_t body_read;
    char* body;               /* malloc(body_len+1). NUL終端する */
} conn_state_t;

/* fd -> conn_state の簡易マップ（fdが大きい環境では調整） */
#define FD_MAP_SIZE 65536
static conn_state_t* g_conn[FD_MAP_SIZE];

static conn_state_t* conn_create(int fd) {
    conn_state_t* c = (conn_state_t*)calloc(1, sizeof(conn_state_t));
    if (!c) return NULL;
    c->fd = fd;
    return c;
}

static void conn_destroy(conn_state_t* c) {
    if (!c) return;
    if (c->body) free(c->body);
    free(c);
}

/* =========================================================
   workerへ渡すタスク（1リクエスト分）
   ========================================================= */
typedef struct {
    int      fd;         /* 返信先fd（workerが返信してcloseする） */
    char*    json;       /* mallocされたJSON（workerがfree） */
    uint32_t json_len;
} task_t;

/* =========================================================
   タスクキュー（スレッドセーフ）
   - キュー満杯のときはpush失敗（過負荷制御）
   ========================================================= */
typedef struct {
    task_t* buf;
    int cap;
    int head;
    int tail;
    int count;
    pthread_mutex_t mtx;
    pthread_cond_t  cond;
} task_queue_t;

static int queue_init(task_queue_t* q, int cap) {
    q->buf = (task_t*)calloc((size_t)cap, sizeof(task_t));
    if (!q->buf) return -1;
    q->cap = cap;
    q->head = q->tail = q->count = 0;
    pthread_mutex_init(&q->mtx, NULL);
    pthread_cond_init(&q->cond, NULL);
    return 0;
}

static int queue_push(task_queue_t* q, task_t t) {
    pthread_mutex_lock(&q->mtx);
    if (q->count >= q->cap) {
        pthread_mutex_unlock(&q->mtx);
        return -1; /* 満杯 */
    }
    q->buf[q->tail] = t;
    q->tail = (q->tail + 1) % q->cap;
    q->count++;
    pthread_cond_signal(&q->cond);
    pthread_mutex_unlock(&q->mtx);
    return 0;
}

static task_t queue_pop(task_queue_t* q) {
    pthread_mutex_lock(&q->mtx);
    while (q->count == 0) {
        pthread_cond_wait(&q->cond, &q->mtx);
    }
    task_t t = q->buf[q->head];
    q->head = (q->head + 1) % q->cap;
    q->count--;
    pthread_mutex_unlock(&q->mtx);
    return t;
}

/* =========================================================
   workerスレッドの文脈（handlerを呼び出すため）
   ========================================================= */
typedef struct {
    const tcp_ipc_server_config_t* cfg;
    tcp_ipc_handler_fn handler;
    void* user_ctx;
    task_queue_t* queue;
} worker_ctx_t;

/* =========================================================
   worker本体
   - handlerでレスポンスJSONを作る
   - 返信も [len][json] で返し close
   ========================================================= */
static void* worker_main(void* arg) {
    worker_ctx_t* w = (worker_ctx_t*)arg;

    while (1) {
        task_t task = queue_pop(w->queue);

        /* ログ用に requestId/threadId を抜く（無くても動く） */
        char requestId[128] = "UNKNOWN";
        long threadId = -1;
        (void)json_get_string(task.json, "requestId", requestId, sizeof(requestId));
        (void)json_get_long(task.json, "threadId", &threadId);

        unsigned long workerTid = (unsigned long)pthread_self();
        long long t0 = now_ms();

        pthread_mutex_lock(&g_log_mutex);
        printf("[WORKER START] t=%lldms port=%d workerTid=%lu fd=%d requestId=%s threadId=%ld\n",
               t0, w->cfg->port, workerTid, task.fd, requestId, threadId);
        pthread_mutex_unlock(&g_log_mutex);

        /* レスポンスバッファ（最大payload+余裕） */
        size_t resp_cap = (size_t)w->cfg->max_payload + 256;
        char* resp = (char*)calloc(1, resp_cap);
        if (!resp) {
            close(task.fd);
            free(task.json);
            continue;
        }

        /* サービス固有処理 */
        int rc = w->handler(task.json, task.json_len, resp, resp_cap, w->user_ctx);

        if (rc != 0) {
            /* handler失敗時の最低限エラー（requestId/threadIdは返す） */
            snprintf(resp, resp_cap,
                     "{\"requestId\":\"%s\",\"threadId\":%ld,\"status\":\"ERROR\",\"code\":\"HANDLER_ERROR\"}",
                     requestId, threadId);
        }

        /* 返信： [4byte length][json] */
        uint32_t resp_len = (uint32_t)strlen(resp);
        uint32_t net_len = htonl(resp_len);

        /* このfdはworkerが専有するので、送信を確実にするためブロッキングに戻す */
        set_blocking(task.fd);
        (void)write_all(task.fd, &net_len, 4);
        (void)write_all(task.fd, resp, resp_len);

        close(task.fd);
        free(task.json);
        free(resp);

        long long t1 = now_ms();
        pthread_mutex_lock(&g_log_mutex);
        printf("[WORKER END]   t=%lldms port=%d workerTid=%lu requestId=%s threadId=%ld elapsed=%lldms\n",
               t1, w->cfg->port, workerTid, requestId, threadId, (t1 - t0));
        pthread_mutex_unlock(&g_log_mutex);
    }
    return NULL;
}

/* =========================================================
   [必須] 受信を進める（分割受信対応）
   - 4byteヘッダを読み切る
   - body_len分を読み切る
   - 読み切ったら epoll から外して task_queue に渡す
   ========================================================= */
static void on_client_readable(int epfd, int fd,
                               const tcp_ipc_server_config_t* cfg,
                               task_queue_t* queue)
{
    if (fd < 0 || fd >= FD_MAP_SIZE) { close(fd); return; }

    conn_state_t* c = g_conn[fd];
    if (!c) {
        c = conn_create(fd);
        g_conn[fd] = c;
    }
    if (!c) { close(fd); return; }

    while (1) {
        /* ---- (1) ヘッダ4byteを読み切る ---- */
        if (c->hdr_read < 4) {
            ssize_t n = read(fd, c->hdr + c->hdr_read, (size_t)(4 - c->hdr_read));
            if (n > 0) {
                c->hdr_read += (int)n;
                if (c->hdr_read < 4) return; /* まだ足りない */

                uint32_t net_len;
                memcpy(&net_len, c->hdr, 4);
                c->body_len = ntohl(net_len);

                /* 防御：サイズ異常は切断 */
                if (c->body_len == 0 || c->body_len > cfg->max_payload) {
                    pthread_mutex_lock(&g_log_mutex);
                    printf("[PROTO ERROR] port=%d fd=%d invalid len=%u\n", cfg->port, fd, c->body_len);
                    pthread_mutex_unlock(&g_log_mutex);
                    close(fd);
                    conn_destroy(c);
                    g_conn[fd] = NULL;
                    return;
                }

                c->body = (char*)malloc((size_t)c->body_len + 1);
                if (!c->body) {
                    close(fd);
                    conn_destroy(c);
                    g_conn[fd] = NULL;
                    return;
                }
                c->body[c->body_len] = '\0';
                c->body_read = 0;
            } else if (n == 0) {
                /* 相手が切断 */
                close(fd);
                conn_destroy(c);
                g_conn[fd] = NULL;
                return;
            } else {
                if (errno == EAGAIN || errno == EWOULDBLOCK) return; /* 次回 */
                if (errno == EINTR) continue;
                close(fd);
                conn_destroy(c);
                g_conn[fd] = NULL;
                return;
            }
        }

        /* ---- (2) ボディを読み切る ---- */
        if (c->hdr_read == 4 && c->body_read < c->body_len) {
            ssize_t n = read(fd, c->body + c->body_read, (size_t)(c->body_len - c->body_read));
            if (n > 0) {
                c->body_read += (uint32_t)n;
                if (c->body_read < c->body_len) continue; /* まだ足りないので継続 */

                /* ---- (3) 受信完了：epollから外し、workerへ渡す ---- */
                epoll_ctl(epfd, EPOLL_CTL_DEL, fd, NULL);

                task_t t;
                t.fd = fd;
                t.json_len = c->body_len;
                t.json = c->body;     /* 所有権をworkerへ移譲 */
                c->body = NULL;

                conn_destroy(c);
                g_conn[fd] = NULL;

                if (queue_push(queue, t) != 0) {
                    /* 過負荷：ここでは切断。必要ならエラー返信の設計に拡張 */
                    close(fd);
                    free(t.json);
                }
                return;
            } else if (n == 0) {
                close(fd);
                conn_destroy(c);
                g_conn[fd] = NULL;
                return;
            } else {
                if (errno == EAGAIN || errno == EWOULDBLOCK) return;
                if (errno == EINTR) continue;
                close(fd);
                conn_destroy(c);
                g_conn[fd] = NULL;
                return;
            }
        }
    }
}

/* =========================================================
   サーバ起動（各サービスのmain.cから呼ぶ）
   ========================================================= */
int tcp_ipc_server_run(const tcp_ipc_server_config_t* cfg,
                       tcp_ipc_handler_fn handler,
                       void* user_ctx)
{
    if (!cfg || !handler) return -1;

    /* ---- (A) サーバソケット作成/bind/listen ---- */
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) { perror("socket"); return -1; }

    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in address;
    memset(&address, 0, sizeof(address));
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons((uint16_t)cfg->port);

    if (bind(server_fd, (struct sockaddr*)&address, sizeof(address)) < 0) {
        perror("bind"); close(server_fd); return -1;
    }
    if (listen(server_fd, cfg->backlog) < 0) {
        perror("listen"); close(server_fd); return -1;
    }

    /* [必須] epollと組み合わせるためlisten fdをノンブロッキング化 */
    set_nonblocking(server_fd);

    /* ---- (B) epoll作成＆server_fd登録 ---- */
    int epfd = epoll_create1(0);
    if (epfd < 0) { perror("epoll_create1"); close(server_fd); return -1; }

    struct epoll_event ev;
    ev.events = EPOLLIN;
    ev.data.fd = server_fd;

    /* [必須] server_fdをepollに登録（新規accept検知） */
    if (epoll_ctl(epfd, EPOLL_CTL_ADD, server_fd, &ev) < 0) {
        perror("epoll_ctl ADD server_fd");
        close(epfd); close(server_fd); return -1;
    }

    /* ---- (C) キュー初期化 ---- */
    task_queue_t queue;
    if (queue_init(&queue, cfg->queue_size) != 0) {
        close(epfd); close(server_fd); return -1;
    }

    /* ---- (D) worker起動 ---- */
    pthread_t* workers = (pthread_t*)calloc((size_t)cfg->worker_count, sizeof(pthread_t));
    worker_ctx_t wctx = { cfg, handler, user_ctx, &queue };

    for (int i = 0; i < cfg->worker_count; i++) {
        pthread_create(&workers[i], NULL, worker_main, &wctx);
    }

    pthread_mutex_lock(&g_log_mutex);
    printf("[SERVER] start port=%d workers=%d protocol=[4byte len][json]\n",
           cfg->port, cfg->worker_count);
    pthread_mutex_unlock(&g_log_mutex);

    /* ---- (E) epoll_wait ループ ---- */
    struct epoll_event* events =
        (struct epoll_event*)calloc((size_t)cfg->max_events, sizeof(struct epoll_event));
    if (!events) {
        close(epfd); close(server_fd); free(workers); return -1;
    }

    while (1) {
        /* [必須] イベント待ち */
        int nfds = epoll_wait(epfd, events, cfg->max_events, -1);
        if (nfds < 0) {
            if (errno == EINTR) continue;
            perror("epoll_wait");
            break;
        }

        for (int i = 0; i < nfds; i++) {
            int fd = events[i].data.fd;

            if (fd == server_fd) {
                /* [必須] ノンブロッキングacceptは「取り尽くす」 */
                while (1) {
                    socklen_t alen = sizeof(address);
                    int client_fd = accept(server_fd, (struct sockaddr*)&address, &alen);
                    if (client_fd < 0) {
                        if (errno == EAGAIN || errno == EWOULDBLOCK) break;
                        perror("accept");
                        break;
                    }

                    /* [必須] clientもノンブロッキング */
                    set_nonblocking(client_fd);

                    if (client_fd < 0 || client_fd >= FD_MAP_SIZE) {
                        close(client_fd);
                        continue;
                    }

                    /* fd状態を初期化（分割受信のため必須） */
                    g_conn[client_fd] = conn_create(client_fd);
                    if (!g_conn[client_fd]) {
                        close(client_fd);
                        continue;
                    }

                    /* [必須] client_fdをepollに登録（受信検知） */
                    ev.events = EPOLLIN;
                    ev.data.fd = client_fd;
                    if (epoll_ctl(epfd, EPOLL_CTL_ADD, client_fd, &ev) < 0) {
                        perror("epoll_ctl ADD client_fd");
                        conn_destroy(g_conn[client_fd]);
                        g_conn[client_fd] = NULL;
                        close(client_fd);
                        continue;
                    }
                }
            } else {
                /* client_fd の受信を進める（分割受信対応） */
                on_client_readable(epfd, fd, cfg, &queue);
            }
        }
    }

    /* 本番では graceful shutdown を実装するのが望ましい */
    free(events);
    close(epfd);
    close(server_fd);
    free(workers);
    return 0;
}