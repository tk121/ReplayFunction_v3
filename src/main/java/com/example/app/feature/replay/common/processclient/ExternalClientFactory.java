package com.example.app.feature.replay.common.processclient;

import com.example.app.feature.replay.common.process.ExternalProcessHandle;

public class ExternalClientFactory {

    public <REQ, RES> ExternalClient<REQ, RES> create(
            ExternalProcessHandle handle,
            Class<RES> responseType,
            int connectTimeoutMillis,
            int readTimeoutMillis) {

        if (handle.isSocket()) {
            return new SocketExternalClient<REQ, RES>(
                    handle.getHost(),
                    handle.getPort().intValue(),
                    responseType,
                    connectTimeoutMillis,
                    readTimeoutMillis);
        }

        if (handle.isFifo()) {
            return new FifoExternalClient<REQ, RES>(
                    handle.getRequestFifoPath(),
                    handle.getResponseFifoPath(),
                    responseType);
        }

        throw new IllegalArgumentException("unsupported transport: " + handle.getTransport());
    }
}