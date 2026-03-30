package com.example.app.feature.replay.event.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReplayEventResponse {
    private Map<String, Map<String, Integer>> series = new LinkedHashMap<String, Map<String, Integer>>();

    public Map<String, Map<String, Integer>> getSeries() { return series; }
    public void setSeries(Map<String, Map<String, Integer>> series) { this.series = series; }
}
