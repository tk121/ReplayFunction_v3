package com.example.app.feature.replay.graphic.c.plant;

/**
 * Plant 用の非同期受付応答です。
 *
 * <p>
 * C サーバが即時返却する ACCEPTED を受けます。
 * </p>
 */
public class PlantAcceptedResponse {

    private String requestId;
    private long threadId;
    private String status;
    private String code;
    private String message;

    public PlantAcceptedResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * ACCEPTED 判定
     */
    public boolean isAccepted() {
        return "ACCEPTED".equalsIgnoreCase(status);
    }
}