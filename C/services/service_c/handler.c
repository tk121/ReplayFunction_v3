#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#include "tcp_ipc_server.h"
#include "cJSON.h"

/*
  ===========================================================
  非同期版 service_a_handler

  要件:
  - requestType = "PLANT_AI_CALC"
  - handler は受付だけして即時 ACCEPTED を返す
  - 実処理は内部ジョブキューへ積んで別スレッドで実行
  - 複数リクエストは順番通り処理
  - 同一リクエスト内の items は並列実行
  - /usr/dev/c_pro は 1件ずつ:
      /usr/dev/c_pro symbol aiValue
  - 結果は呼び出し元へ返さない
  ===========================================================
*/

#define C_PRO_PATH "/usr/dev/c_pro"
#define SYMBOL_BUF_SIZE 128
#define REQUEST_ID_BUF_SIZE 128
#define REQUEST_TYPE_BUF_SIZE 64

typedef struct {
    char symbol[SYMBOL_BUF_SIZE];
    double ai_value;
} request_item_t;

typedef struct async_job {
    char request_id[REQUEST_ID_BUF_SIZE];
    long thread_id;
    char request_type[REQUEST_TYPE_BUF_SIZE];

    request_item_t* items;
    int item_count;

    struct async_job* next;
} async_job_t;

typedef struct {
    pthread_t executor_thread;
    pthread_mutex_t mutex;
    pthread_cond_t cond;

    async_job_t* head;
    async_job_t* tail;

    int stop;
} service_a_async_context_t;

typedef struct {
    pid_t pid;
    char symbol[SYMBOL_BUF_SIZE];
    double ai_value;
    int spawn_rc;
} child_proc_t;

/* =========================================================
   JSON utility
   ========================================================= */
static int get_json_string(cJSON* obj, const char* key, const char** out_value)
{
    cJSON* item = cJSON_GetObjectItemCaseSensitive(obj, key);
    if (!cJSON_IsString(item) || item->valuestring == NULL) {
        return 0;
    }
    *out_value = item->valuestring;
    return 1;
}

static int get_json_number(cJSON* obj, const char* key, double* out_value)
{
    cJSON* item = cJSON_GetObjectItemCaseSensitive(obj, key);
    if (!cJSON_IsNumber(item)) {
        return 0;
    }
    *out_value = item->valuedouble;
    return 1;
}

/* =========================================================
   ACCEPTED response
   ========================================================= */
static int build_accepted_response(char* resp_json, size_t resp_cap,
                                   const char* request_id,
                                   long thread_id)
{
    cJSON* root = NULL;
    char* json_str = NULL;
    int rc = -1;

    root = cJSON_CreateObject();
    if (root == NULL) {
        return -1;
    }

    cJSON_AddStringToObject(root, "requestId", request_id != NULL ? request_id : "");
    cJSON_AddNumberToObject(root, "threadId", thread_id);
    cJSON_AddStringToObject(root, "status", "ACCEPTED");

    json_str = cJSON_PrintUnformatted(root);
    if (json_str == NULL) {
        goto cleanup;
    }

    if (strlen(json_str) + 1 > resp_cap) {
        goto cleanup;
    }

    snprintf(resp_json, resp_cap, "%s", json_str);
    rc = 0;

cleanup:
    if (json_str != NULL) {
        cJSON_free(json_str);
    }
    cJSON_Delete(root);
    return rc;
}

static int build_error_response(char* resp_json, size_t resp_cap,
                                const char* request_id,
                                long thread_id,
                                const char* code,
                                const char* message)
{
    cJSON* root = NULL;
    char* json_str = NULL;
    int rc = -1;

    root = cJSON_CreateObject();
    if (root == NULL) {
        return -1;
    }

    cJSON_AddStringToObject(root, "requestId", request_id != NULL ? request_id : "");
    cJSON_AddNumberToObject(root, "threadId", thread_id);
    cJSON_AddStringToObject(root, "status", "ERROR");
    cJSON_AddStringToObject(root, "code", code != NULL ? code : "UNKNOWN_ERROR");
    cJSON_AddStringToObject(root, "message", message != NULL ? message : "");

    json_str = cJSON_PrintUnformatted(root);
    if (json_str == NULL) {
        goto cleanup;
    }

    if (strlen(json_str) + 1 > resp_cap) {
        goto cleanup;
    }

    snprintf(resp_json, resp_cap, "%s", json_str);
    rc = 0;

cleanup:
    if (json_str != NULL) {
        cJSON_free(json_str);
    }
    cJSON_Delete(root);
    return rc;
}

/* =========================================================
   async job utility
   ========================================================= */
static void free_async_job(async_job_t* job)
{
    if (job == NULL) {
        return;
    }
    if (job->items != NULL) {
        free(job->items);
    }
    free(job);
}

static int enqueue_job(service_a_async_context_t* ctx, async_job_t* job)
{
    if (ctx == NULL || job == NULL) {
        return -1;
    }

    job->next = NULL;

    pthread_mutex_lock(&ctx->mutex);

    if (ctx->tail == NULL) {
        ctx->head = job;
        ctx->tail = job;
    } else {
        ctx->tail->next = job;
        ctx->tail = job;
    }

    pthread_cond_signal(&ctx->cond);
    pthread_mutex_unlock(&ctx->mutex);
    return 0;
}

static async_job_t* dequeue_job(service_a_async_context_t* ctx)
{
    async_job_t* job = NULL;

    pthread_mutex_lock(&ctx->mutex);

    while (!ctx->stop && ctx->head == NULL) {
        pthread_cond_wait(&ctx->cond, &ctx->mutex);
    }

    if (ctx->stop) {
        pthread_mutex_unlock(&ctx->mutex);
        return NULL;
    }

    job = ctx->head;
    ctx->head = job->next;
    if (ctx->head == NULL) {
        ctx->tail = NULL;
    }

    pthread_mutex_unlock(&ctx->mutex);
    return job;
}

/* =========================================================
   /usr/dev/c_pro execution
   ========================================================= */
static int spawn_c_pro(const char* symbol, double ai_value, child_proc_t* out_child)
{
    pid_t pid;
    char ai_value_str[64];

    if (symbol == NULL || out_child == NULL) {
        return -1;
    }

    snprintf(ai_value_str, sizeof(ai_value_str), "%.6f", ai_value);

    pid = fork();
    if (pid < 0) {
        return -2;
    }

    if (pid == 0) {
        execl(C_PRO_PATH,
              C_PRO_PATH,
              symbol,
              ai_value_str,
              (char*)NULL);
        _exit(127);
    }

    out_child->pid = pid;
    snprintf(out_child->symbol, sizeof(out_child->symbol), "%s", symbol);
    out_child->ai_value = ai_value;
    out_child->spawn_rc = 0;

    return 0;
}

static int wait_c_pro(child_proc_t* child)
{
    int status;

    if (child == NULL) {
        return -1;
    }

    if (waitpid(child->pid, &status, 0) < 0) {
        return -2;
    }

    if (WIFEXITED(status)) {
        return WEXITSTATUS(status);
    }

    if (WIFSIGNALED(status)) {
        return 128 + WTERMSIG(status);
    }

    return -3;
}

/* =========================================================
   one request execution
   - request間は executor thread が1本なので必ず直列
   - request内 items は先に全部 spawn して並列
   ========================================================= */
static void execute_one_job(async_job_t* job)
{
    int i;
    child_proc_t* children = NULL;

    if (job == NULL) {
        return;
    }

    printf("[ASYNC START] requestId=%s threadId=%ld itemCount=%d\n",
           job->request_id, job->thread_id, job->item_count);

    if (job->item_count <= 0) {
        printf("[ASYNC END] requestId=%s no items\n", job->request_id);
        return;
    }

    children = (child_proc_t*)calloc((size_t)job->item_count, sizeof(child_proc_t));
    if (children == NULL) {
        printf("[ASYNC ERROR] requestId=%s failed to allocate children\n", job->request_id);
        return;
    }

    for (i = 0; i < job->item_count; i++) {
        children[i].pid = -1;
        children[i].spawn_rc = -999;
        children[i].symbol[0] = '\0';
        children[i].ai_value = 0.0;
    }

    /*
      第1段階:
      同一 request 内の items を全部起動
      -> ここは並列でよい
    */
    for (i = 0; i < job->item_count; i++) {
        int rc = spawn_c_pro(job->items[i].symbol, job->items[i].ai_value, &children[i]);
        if (rc != 0) {
            children[i].spawn_rc = rc;
            snprintf(children[i].symbol, sizeof(children[i].symbol), "%s", job->items[i].symbol);
            children[i].ai_value = job->items[i].ai_value;
            printf("[ASYNC SPAWN ERROR] requestId=%s index=%d symbol=%s rc=%d\n",
                   job->request_id, i, job->items[i].symbol, rc);
            continue;
        }

        printf("[ASYNC SPAWN] requestId=%s index=%d symbol=%s aiValue=%.6f pid=%d\n",
               job->request_id, i, children[i].symbol, children[i].ai_value, (int)children[i].pid);
    }

    /*
      第2段階:
      全 child 終了待ち
      -> この request が終わるまで次 request へ進まない
    */
    for (i = 0; i < job->item_count; i++) {
        int exit_rc;

        if (children[i].spawn_rc != 0 || children[i].pid <= 0) {
            continue;
        }

        exit_rc = wait_c_pro(&children[i]);

        printf("[ASYNC WAIT] requestId=%s index=%d symbol=%s exit=%d\n",
               job->request_id, i, children[i].symbol, exit_rc);
    }

    free(children);

    printf("[ASYNC END] requestId=%s threadId=%ld done\n",
           job->request_id, job->thread_id);
}

/* =========================================================
   executor thread
   ========================================================= */
static void* executor_main(void* arg)
{
    service_a_async_context_t* ctx = (service_a_async_context_t*)arg;

    while (1) {
        async_job_t* job = dequeue_job(ctx);
        if (job == NULL) {
            if (ctx->stop) {
                break;
            }
            continue;
        }

        execute_one_job(job);
        free_async_job(job);
    }

    return NULL;
}

/* =========================================================
   context create/destroy
   ========================================================= */
void* service_a_async_context_create(void)
{
    service_a_async_context_t* ctx;

    ctx = (service_a_async_context_t*)calloc(1, sizeof(service_a_async_context_t));
    if (ctx == NULL) {
        return NULL;
    }

    pthread_mutex_init(&ctx->mutex, NULL);
    pthread_cond_init(&ctx->cond, NULL);

    ctx->head = NULL;
    ctx->tail = NULL;
    ctx->stop = 0;

    if (pthread_create(&ctx->executor_thread, NULL, executor_main, ctx) != 0) {
        pthread_cond_destroy(&ctx->cond);
        pthread_mutex_destroy(&ctx->mutex);
        free(ctx);
        return NULL;
    }

    return ctx;
}

void service_a_async_context_destroy(void* pctx)
{
    service_a_async_context_t* ctx = (service_a_async_context_t*)pctx;
    async_job_t* cur;
    async_job_t* next;

    if (ctx == NULL) {
        return;
    }

    pthread_mutex_lock(&ctx->mutex);
    ctx->stop = 1;
    pthread_cond_broadcast(&ctx->cond);
    pthread_mutex_unlock(&ctx->mutex);

    pthread_join(ctx->executor_thread, NULL);

    cur = ctx->head;
    while (cur != NULL) {
        next = cur->next;
        free_async_job(cur);
        cur = next;
    }

    pthread_cond_destroy(&ctx->cond);
    pthread_mutex_destroy(&ctx->mutex);
    free(ctx);
}

/* =========================================================
   request parse -> async_job
   ========================================================= */
static async_job_t* parse_request_to_job(const char* req_json,
                                         char* error_code, size_t error_code_size,
                                         char* error_message, size_t error_message_size,
                                         char* request_id_buf, size_t request_id_buf_size,
                                         long* out_thread_id)
{
    cJSON* root = NULL;
    cJSON* items = NULL;
    async_job_t* job = NULL;
    const char* request_id = "";
    const char* request_type = "";
    long thread_id = -1;
    int item_count = 0;
    int i;

    if (req_json == NULL) {
        snprintf(error_code, error_code_size, "INVALID_JSON");
        snprintf(error_message, error_message_size, "req_json is NULL");
        return NULL;
    }

    root = cJSON_Parse(req_json);
    if (root == NULL) {
        snprintf(error_code, error_code_size, "INVALID_JSON");
        snprintf(error_message, error_message_size, "Failed to parse request JSON");
        return NULL;
    }

    if (!get_json_string(root, "requestId", &request_id)) {
        request_id = "";
    }
    snprintf(request_id_buf, request_id_buf_size, "%s", request_id);

    {
        cJSON* thread_item = cJSON_GetObjectItemCaseSensitive(root, "threadId");
        if (cJSON_IsNumber(thread_item)) {
            thread_id = (long)thread_item->valuedouble;
        }
    }
    *out_thread_id = thread_id;

    if (!get_json_string(root, "requestType", &request_type)) {
        snprintf(error_code, error_code_size, "INVALID_REQUEST");
        snprintf(error_message, error_message_size, "requestType is missing or invalid");
        cJSON_Delete(root);
        return NULL;
    }

    if (strcmp(request_type, "PLANT_AI_CALC") != 0) {
        snprintf(error_code, error_code_size, "UNSUPPORTED_REQUEST_TYPE");
        snprintf(error_message, error_message_size, "requestType is not supported");
        cJSON_Delete(root);
        return NULL;
    }

    items = cJSON_GetObjectItemCaseSensitive(root, "items");
    if (!cJSON_IsArray(items)) {
        snprintf(error_code, error_code_size, "INVALID_REQUEST");
        snprintf(error_message, error_message_size, "items must be an array");
        cJSON_Delete(root);
        return NULL;
    }

    item_count = cJSON_GetArraySize(items);

    job = (async_job_t*)calloc(1, sizeof(async_job_t));
    if (job == NULL) {
        snprintf(error_code, error_code_size, "INTERNAL_ERROR");
        snprintf(error_message, error_message_size, "Failed to allocate async job");
        cJSON_Delete(root);
        return NULL;
    }

    snprintf(job->request_id, sizeof(job->request_id), "%s", request_id);
    snprintf(job->request_type, sizeof(job->request_type), "%s", request_type);
    job->thread_id = thread_id;
    job->item_count = item_count;
    job->next = NULL;

    if (item_count > 0) {
        job->items = (request_item_t*)calloc((size_t)item_count, sizeof(request_item_t));
        if (job->items == NULL) {
            snprintf(error_code, error_code_size, "INTERNAL_ERROR");
            snprintf(error_message, error_message_size, "Failed to allocate request items");
            free_async_job(job);
            cJSON_Delete(root);
            return NULL;
        }
    }

    for (i = 0; i < item_count; i++) {
        cJSON* item = cJSON_GetArrayItem(items, i);
        const char* symbol = NULL;
        double ai_value = 0.0;

        if (!cJSON_IsObject(item)) {
            snprintf(error_code, error_code_size, "INVALID_REQUEST");
            snprintf(error_message, error_message_size, "item is not an object");
            free_async_job(job);
            cJSON_Delete(root);
            return NULL;
        }

        if (!get_json_string(item, "symbol", &symbol)) {
            snprintf(error_code, error_code_size, "INVALID_REQUEST");
            snprintf(error_message, error_message_size, "symbol is missing or invalid");
            free_async_job(job);
            cJSON_Delete(root);
            return NULL;
        }

        if (!get_json_number(item, "aiValue", &ai_value)) {
            snprintf(error_code, error_code_size, "INVALID_REQUEST");
            snprintf(error_message, error_message_size, "aiValue is missing or invalid");
            free_async_job(job);
            cJSON_Delete(root);
            return NULL;
        }

        snprintf(job->items[i].symbol, sizeof(job->items[i].symbol), "%s", symbol);
        job->items[i].ai_value = ai_value;
    }

    cJSON_Delete(root);
    return job;
}

/* =========================================================
   public handler
   - parse
   - enqueue
   - return ACCEPTED immediately
   ========================================================= */
int service_a_handler(const char* req_json, uint32_t req_len,
                      char* resp_json, size_t resp_cap,
                      void* user_ctx)
{
    service_a_async_context_t* ctx = (service_a_async_context_t*)user_ctx;
    async_job_t* job = NULL;
    char error_code[64];
    char error_message[256];
    char request_id[REQUEST_ID_BUF_SIZE];
    long thread_id = -1;

    (void)req_len;

    if (ctx == NULL || resp_json == NULL || resp_cap == 0) {
        return -1;
    }

    memset(error_code, 0, sizeof(error_code));
    memset(error_message, 0, sizeof(error_message));
    memset(request_id, 0, sizeof(request_id));

    job = parse_request_to_job(req_json,
                               error_code, sizeof(error_code),
                               error_message, sizeof(error_message),
                               request_id, sizeof(request_id),
                               &thread_id);

    if (job == NULL) {
        return build_error_response(resp_json, resp_cap,
                                    request_id, thread_id,
                                    error_code[0] ? error_code : "INVALID_REQUEST",
                                    error_message[0] ? error_message : "Invalid request");
    }

    if (enqueue_job(ctx, job) != 0) {
        free_async_job(job);
        return build_error_response(resp_json, resp_cap,
                                    request_id, thread_id,
                                    "QUEUE_ERROR",
                                    "Failed to enqueue async job");
    }

    return build_accepted_response(resp_json, resp_cap, request_id, thread_id);
}