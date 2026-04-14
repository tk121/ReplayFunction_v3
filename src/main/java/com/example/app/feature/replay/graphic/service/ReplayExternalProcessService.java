package com.example.app.feature.replay.graphic.service;

import com.example.app.feature.replay.graphic.dto.external.GraphicProcessResponse;
import com.example.app.feature.replay.graphic.dto.external.TrendProcessResponse;

public class ReplayExternalProcessService {

    private final GraphicProcessService graphicProcessService;
    private final TrendProcessService trendProcessService;

    public ReplayExternalProcessService(
            GraphicProcessService graphicProcessService,
            TrendProcessService trendProcessService) {
        this.graphicProcessService = graphicProcessService;
        this.trendProcessService = trendProcessService;
    }

    public GraphicProcessResponse executeGraphic(String command, String replayTime) throws Exception {
        return graphicProcessService.execute(command, replayTime);
    }

    public TrendProcessResponse executeTrend(String command, String replayTime) throws Exception {
        return trendProcessService.execute(command, replayTime);
    }
}