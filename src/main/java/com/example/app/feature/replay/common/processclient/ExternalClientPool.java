package com.example.app.feature.replay.common.processclient;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExternalClientPool<REQ, RES> implements ExternalClient<REQ, RES> {

    private final List<ExternalClient<REQ, RES>> clients;
    private final AtomicInteger sequence = new AtomicInteger(0);

    public ExternalClientPool(List<ExternalClient<REQ, RES>> clients) {
        if (clients == null || clients.isEmpty()) {
            throw new IllegalArgumentException("clients must not be empty");
        }
        this.clients = clients;
    }

    @Override
    public RES send(REQ request) throws Exception {
        int index = Math.abs(sequence.getAndIncrement()) % clients.size();
        return clients.get(index).send(request);
    }
}