package com.example.app.feature.replay.graphic.c.replay;

/**
 * Replay 系 C プロセス向けの受信DTO例です。
 */
public class ReplayResponse {

    private String requestId;
    private String status;
    private String message;

    public ReplayResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
