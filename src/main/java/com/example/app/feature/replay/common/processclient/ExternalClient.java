package com.example.app.feature.replay.common.processclient;

public interface ExternalClient<REQ, RES> {

    RES send(REQ request) throws Exception;
}