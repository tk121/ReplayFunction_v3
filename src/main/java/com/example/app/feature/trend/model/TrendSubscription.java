package com.example.app.feature.trend.model;

import java.time.LocalDateTime;

public class TrendSubscription {

    private String userId;
    private String clientId;
    private String wsSessionId;
    private Long trendId;
    private LocalDateTime lastDeliveredTime;
    private boolean realtime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getWsSessionId() {
        return wsSessionId;
    }

    public void setWsSessionId(String wsSessionId) {
        this.wsSessionId = wsSessionId;
    }

    public Long getTrendId() {
        return trendId;
    }

    public void setTrendId(Long trendId) {
        this.trendId = trendId;
    }

    public LocalDateTime getLastDeliveredTime() {
        return lastDeliveredTime;
    }

    public void setLastDeliveredTime(LocalDateTime lastDeliveredTime) {
        this.lastDeliveredTime = lastDeliveredTime;
    }

    public boolean isRealtime() {
        return realtime;
    }

    public void setRealtime(boolean realtime) {
        this.realtime = realtime;
    }
}