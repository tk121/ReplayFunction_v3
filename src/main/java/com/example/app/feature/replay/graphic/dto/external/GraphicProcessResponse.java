package com.example.app.feature.replay.graphic.dto.external;

public class GraphicProcessResponse {

    private String status;
    private String message;

    public GraphicProcessResponse() {
    }

    public boolean isSuccess() {
        return "OK".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status);
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