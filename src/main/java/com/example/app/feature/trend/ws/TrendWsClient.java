package com.example.app.feature.trend.ws;

import javax.websocket.Session;

public class TrendWsClient {

    private final Session session;
    private final String userId;
    private final String clientId;

    public TrendWsClient(Session session, String userId, String clientId) {
        this.session = session;
        this.userId = userId;
        this.clientId = clientId;
    }

    public Session getSession() {
        return session;
    }

    public String getUserId() {
        return userId;
    }

    public String getClientId() {
        return clientId;
    }
}
