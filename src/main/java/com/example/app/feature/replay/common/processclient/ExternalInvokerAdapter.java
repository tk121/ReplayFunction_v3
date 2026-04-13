package com.example.app.feature.replay.common.processclient;

import com.example.app.feature.replay.graphic.external.ExternalInvoker;

public class ExternalInvokerAdapter<REQ, RES> implements ExternalInvoker<REQ, RES> {

    private final ExternalClient<REQ, RES> client;

    public ExternalInvokerAdapter(ExternalClient<REQ, RES> client) {
        this.client = client;
    }

    @Override
    public RES invoke(REQ request) throws Exception {
        return client.send(request);
    }
}