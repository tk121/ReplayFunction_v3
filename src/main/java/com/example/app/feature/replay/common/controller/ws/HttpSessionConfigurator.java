package com.example.app.feature.replay.common.controller.ws;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * WebSocket ハンドシェイク時に HttpSession を Endpoint へ引き渡すための Configurator です。
 */
public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    public static final String HTTP_SESSION_KEY = "httpSession";

    @Override
    public void modifyHandshake(
            ServerEndpointConfig sec,
            HandshakeRequest request,
            HandshakeResponse response) {

        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession != null) {
            sec.getUserProperties().put(HTTP_SESSION_KEY, httpSession);
        }
    }
}
