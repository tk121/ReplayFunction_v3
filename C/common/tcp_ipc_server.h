#pragma once
#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/* =========================================================
   TCP IPC Server (length-prefixed JSON protocol)
   - Protocol: [4byte length (big-endian)][UTF-8 JSON bytes]
   - epoll + non-blocking accept/read
   - task queue + worker threads
   ========================================================= */

/* サービス固有処理（ここだけ差し替えて流用する） */
typedef int (*tcp_ipc_handler_fn)(
    const char* req_json,      /* 受信したJSON（UTF-8, NUL終端あり） */
    uint32_t    req_len,       /* JSONバイト長 */
    char*       resp_json,     /* レスポンスJSONを書き込むバッファ */
    size_t      resp_cap,      /* resp_json の容量 */
    void*       user_ctx       /* サービス固有の状態（設定/DBハンドル等） */
);

/* サービスごとの設定（main.cで指定） */
typedef struct {
    int      port;             /* 待受ポート（サービス毎に固定） */
    int      worker_count;     /* workerスレッド数（例: 4〜16） */
    int      max_events;       /* epoll_wait で一度に受けるイベント数 */
    uint32_t max_payload;      /* 受信JSON最大サイズ（防御） */
    int      queue_size;       /* タスクキューサイズ（防御） */
    int      backlog;          /* listen backlog */
} tcp_ipc_server_config_t;

/* 起動（通常は常駐するので戻らない。異常時は !=0 を返す） */
int tcp_ipc_server_run(const tcp_ipc_server_config_t* cfg,
                       tcp_ipc_handler_fn handler,
                       void* user_ctx);

#ifdef __cplusplus
}
#endif