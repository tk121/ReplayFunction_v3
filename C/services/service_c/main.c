#include <stdio.h>
#include <stdlib.h>

#include "tcp_ipc_server.h"

/* handler 宣言 */
int service_a_handler(const char* req_json, uint32_t req_len,
                      char* resp_json, size_t resp_cap,
                      void* user_ctx);

/* 非同期実行コンテキスト初期化/終了 */
void* service_a_async_context_create(void);
void  service_a_async_context_destroy(void* ctx);

int main(void) {
    tcp_ipc_server_config_t cfg;
    void* async_ctx;

    async_ctx = service_a_async_context_create();
    if (async_ctx == NULL) {
        fprintf(stderr, "failed to create async context\n");
        return 1;
    }

    cfg.port = 5000;

    /*
      通信の worker は複数でも動くが、
      今回はまず分かりやすさ重視で 1 にしておく。
      実処理順序は handler 内部のジョブキューで保証する。
    */
    cfg.worker_count = 1;

    cfg.max_events = 100;
    cfg.max_payload = 1024 * 1024; /* 1MB */
    cfg.queue_size = 1000;
    cfg.backlog = 128;

    /*
      通常は戻らない。
      戻るのは異常終了時のみ。
    */
    {
        int rc = tcp_ipc_server_run(&cfg, service_a_handler, async_ctx);
        service_a_async_context_destroy(async_ctx);
        return rc;
    }
}