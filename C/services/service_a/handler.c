#include <stdio.h>
#include <string.h>
#include "tcp_ipc_server.h"

/*
  ここが「サービス固有処理」。
  - req_json を見て処理し、resp_json に JSON を書く
  - 本番は JSON パースをきちんとやる（cJSON等）推奨
*/
int service_a_handler(const char* req_json, uint32_t req_len,
                      char* resp_json, size_t resp_cap,
                      void* user_ctx)
{
    (void)req_len;
    (void)user_ctx;

    if (strstr(req_json, "processA") != NULL) {
        snprintf(resp_json, resp_cap,
                 "{\"status\":\"OK\",\"result\":{\"message\":\"ServiceA: Processed A\"}}");
        return 0;
    }
    if (strstr(req_json, "processB") != NULL) {
        snprintf(resp_json, resp_cap,
                 "{\"status\":\"OK\",\"result\":{\"message\":\"ServiceA: Processed B\"}}");
        return 0;
    }

    snprintf(resp_json, resp_cap,
             "{\"status\":\"ERROR\",\"code\":\"UNKNOWN_COMMAND\"}");
    return 0;
}