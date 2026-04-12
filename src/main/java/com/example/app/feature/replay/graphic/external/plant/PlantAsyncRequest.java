package com.example.app.feature.replay.graphic.external.plant;

import java.util.List;

/**
 * Plant 用の非同期 C リクエストです。
 */
public class PlantAsyncRequest {

    private String requestId;
    private long threadId;
    private String requestType;
    private List<PlantAsyncRequestItem> items;

    public PlantAsyncRequest() {
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public List<PlantAsyncRequestItem> getItems() {
        return items;
    }

    public void setItems(List<PlantAsyncRequestItem> items) {
        this.items = items;
    }
}