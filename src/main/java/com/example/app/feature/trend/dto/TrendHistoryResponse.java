package com.example.app.feature.trend.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.app.feature.trend.model.TrendDataPoint;

/**
 * trend 過去表示用レスポンスです。
 *
 * <p>
 * key=deviceId, value=その device の時系列データ一覧
 * </p>
 */
public class TrendHistoryResponse {

    private Long trendId;
    private String trendName;
    private String targetDate;
    private Map<String, List<TrendDataPoint>> series =
            new LinkedHashMap<String, List<TrendDataPoint>>();

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

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public Map<String, List<TrendDataPoint>> getSeries() {
        return series;
    }

    public void setSeries(Map<String, List<TrendDataPoint>> series) {
        this.series = series;
    }
}