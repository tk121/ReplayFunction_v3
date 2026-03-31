#include "tcp_ipc_server.h"

int service_b_handler(const char* req_json, uint32_t req_len,
                      char* resp_json, size_t resp_cap,
                      void* user_ctx);

int main(void) {
    tcp_ipc_server_config_t cfg;

    /* サービスBは5001番 */
    cfg.port         = 5001;
    cfg.worker_count = 4;
    cfg.max_events   = 100;
    cfg.max_payload  = 1024 * 1024;
    cfg.queue_size   = 1000;
    cfg.backlog      = 128;

    return tcp_ipc_server_run(&cfg, service_b_handler, NULL);
}