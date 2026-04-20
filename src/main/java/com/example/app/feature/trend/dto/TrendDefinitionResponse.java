package com.example.app.feature.trend.dto;

import java.util.ArrayList;
import java.util.List;

public class TrendDefinitionResponse {

    private Long trendId;
    private String trendName;
    private List<String> deviceIds = new ArrayList<String>();

    public Long getTrendId() {
        return trendId;
    }

    public void setTrendId(Long trendId) {
        this.trendId = trendId;
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
