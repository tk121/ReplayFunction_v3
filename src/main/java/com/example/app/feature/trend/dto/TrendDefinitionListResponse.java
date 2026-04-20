package com.example.app.feature.trend.dto;

import java.util.ArrayList;
import java.util.List;

public class TrendDefinitionListResponse {

    private List<TrendDefinitionResponse> trends =
            new ArrayList<TrendDefinitionResponse>();

    public List<TrendDefinitionResponse> getTrends() {
        return trends;
    }

    public void setTrends(List<TrendDefinitionResponse> trends) {
        this.trends = trends;
    }
}