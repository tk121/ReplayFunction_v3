package com.example.app.feature.trend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * trend グラフ用の1点データです。
 */
public class TrendDataPoint {

    private String deviceId;
    private LocalDateTime occurredAt;
    private BigDecimal value;

    public TrendDataPoint() {
    }

    public TrendDataPoint(String deviceId, LocalDateTime occurredAt, BigDecimal value) {
        this.deviceId = deviceId;
        this.occurredAt = occurredAt;
        this.value = value;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
