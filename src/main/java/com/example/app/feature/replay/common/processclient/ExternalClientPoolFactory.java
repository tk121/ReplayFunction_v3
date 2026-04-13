package com.example.app.feature.replay.common.processclient;

import java.util.ArrayList;
import java.util.List;

import com.example.app.feature.replay.common.process.ExternalProcessHandle;

public class ExternalClientPoolFactory {

    private final ExternalClientFactory clientFactory = new ExternalClientFactory();

    public <REQ, RES> ExternalClientPool<REQ, RES> createPool(
            List<ExternalProcessHandle> handles,
            Class<RES> responseType,
            int connectTimeoutMillis,
            int readTimeoutMillis) {

        List<ExternalClient<REQ, RES>> clients = new ArrayList<ExternalClient<REQ, RES>>();
        for (ExternalProcessHandle handle : handles) {
            clients.add(clientFactory.<REQ, RES>create(
                    handle,
                    responseType,
                    connectTimeoutMillis,
                    readTimeoutMillis));
        }
        return new ExternalClientPool<REQ, RES>(clients);
    }
}