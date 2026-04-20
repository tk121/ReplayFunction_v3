package com.example.app.feature.trend.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.app.feature.trend.model.TrendDataPoint;

/**
 * trend realtime 差分レスポンスです。
 *
 * <p>
 * key=deviceId, value=差分データ一覧
 * </p>
 */
public class TrendDeltaResponse {

    private Long trendId;
    private Map<String, List<TrendDataPoint>> series =
            new LinkedHashMap<String, List<TrendDataPoint>>();

    public Long getTrendId() {
        return trendId;
    }

    public void setTrendId(Long trendId) {
        this.trendId = trendId;
    }

    public Map<String, List<TrendDataPoint>> getSeries() {
        return series;
    }

    public void setSeries(Map<String, List<TrendDataPoint>> series) {
        this.series = series;
    }

    public void ensureDevice(String deviceId) {
        if (!series.containsKey(deviceId)) {
            series.put(deviceId, new ArrayList<TrendDataPoint>());
        }
    }
}
