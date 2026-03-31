#include <stdio.h>
#include <string.h>
#include "tcp_ipc_server.h"

int service_b_handler(const char* req_json, uint32_t req_len,
                      char* resp_json, size_t resp_cap,
                      void* user_ctx)
{
    (void)req_len;
    (void)user_ctx;

    /* 例：serviceBは別の処理体系 */
    if (strstr(req_json, "processX") != NULL) {
        snprintf(resp_json, resp_cap,
                 "{\"status\":\"OK\",\"result\":{\"message\":\"ServiceB: Processed X\"}}");
        return 0;
    }

    snprintf(resp_json, resp_cap,
             "{\"status\":\"ERROR\",\"code\":\"UNKNOWN_COMMAND\"}");
    return 0;
}