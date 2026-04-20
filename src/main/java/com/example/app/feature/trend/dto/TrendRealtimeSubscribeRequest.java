package com.example.app.feature.trend.dto;

public class TrendRealtimeSubscribeRequest {

    private Long trendId;
    private String clientId;

    public Long getTrendId() {
        return trendId;
    }

    public void setTrendId(Long trendId) {
        this.trendId = trendId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}