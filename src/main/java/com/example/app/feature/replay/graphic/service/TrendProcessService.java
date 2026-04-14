package com.example.app.feature.replay.graphic.service;

import com.example.app.feature.replay.common.processclient.ExternalClient;
import com.example.app.feature.replay.graphic.dto.external.TrendProcessRequest;
import com.example.app.feature.replay.graphic.dto.external.TrendProcessResponse;

public class TrendProcessService {

    private final ExternalClient<TrendProcessRequest, TrendProcessResponse> client;

    public TrendProcessService(
            ExternalClient<TrendProcessRequest, TrendProcessResponse> client) {
        this.client = client;
    }

    public TrendProcessResponse execute(String command, String replayTime) throws Exception {
        TrendProcessRequest request = new TrendProcessRequest(command, replayTime);
        return client.send(request);
    }
}