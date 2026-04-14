package com.example.app.feature.replay.graphic.service;

import com.example.app.feature.replay.common.processclient.ExternalClient;
import com.example.app.feature.replay.graphic.dto.external.GraphicProcessRequest;
import com.example.app.feature.replay.graphic.dto.external.GraphicProcessResponse;

public class GraphicProcessService {

    private final ExternalClient<GraphicProcessRequest, GraphicProcessResponse> client;

    public GraphicProcessService(
            ExternalClient<GraphicProcessRequest, GraphicProcessResponse> client) {
        this.client = client;
    }

    public GraphicProcessResponse execute(String command, String replayTime) throws Exception {
        GraphicProcessRequest request = new GraphicProcessRequest(command, replayTime);
        return client.send(request);
    }
}