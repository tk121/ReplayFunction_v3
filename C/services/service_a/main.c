#include "tcp_ipc_server.h"

/* handler宣言 */
int service_a_handler(const char* req_json, uint32_t req_len,
                      char* resp_json, size_t resp_cap,
                      void* user_ctx);

int main(void) {
    tcp_ipc_server_config_t cfg;

    /* サービスAは5000番で待受 */
    cfg.port         = 5000;

    /* 同時100程度ならまずは4〜8で開始がおすすめ */
    cfg.worker_count = 4;

    /* epoll_waitが一度に受けるイベント数 */
    cfg.max_events   = 100;

    /* 受信JSON最大（防御） */
    cfg.max_payload  = 1024 * 1024; /* 1MB */

    /* キュー上限（過負荷制御） */
    cfg.queue_size   = 1000;

    /* listen backlog */
    cfg.backlog      = 128;

    return tcp_ipc_server_run(&cfg, service_a_handler, NULL);
}