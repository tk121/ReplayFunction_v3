package com.example.app.feature.trend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ユーザが登録した trend 定義です。
 *
 * <p>
 * 1つの trend は、ユーザが device を最大10件まで選択して構成します。
 * </p>
 */
public class TrendDefinition {

    private Long trendId;
    private String userId;
    private String trendName;
    private List<String> deviceIds = new ArrayList<String>();

    public Long getTrendId() {
        return trendId;
    }

    public void setTrendId(Long trendId) {
        this.trendId = trendId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTrendName() {
        return trendName;
    }

    public void setTrendName(String trendName) {
        this.trendName = trendName;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }
}