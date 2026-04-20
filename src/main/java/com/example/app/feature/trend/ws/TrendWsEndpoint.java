package com.example.app.feature.trend.ws;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.controller.ws.HttpSessionConfigurator;

@ServerEndpoint(value = "/ws/trend", configurator = HttpSessionConfigurator.class)
public class TrendWsEndpoint {

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        try {
            HttpSession httpSession = (HttpSession) config.getUserProperties()
                    .get(HttpSessionConfigurator.HTTP_SESSION_KEY);

            LoginUser loginUser = null;
            if (httpSession != null) {
                loginUser = (LoginUser) httpSession.getAttribute("replay.loginUser");
            }

            if (loginUser == null) {
                session.close();
                return;
            }

            String clientId = getQueryParam(session, "clientId");
            if (clientId == null || clientId.trim().length() == 0) {
                session.close();
                return;
            }

            TrendWsClient client = new TrendWsClient(
                    session,
                    loginUser.getUserId(),
                    clientId);

            AppRuntime.getTrendModule()
                    .getTrendWsHub()
                    .register(client);

            AppRuntime.getTrendModule()
                    .getTrendWsHub()
                    .bindWsSessionId(loginUser.getUserId(), clientId, session.getId());

        } catch (Exception e) {
            try {
                session.close();
            } catch (Exception ignore) {
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        AppRuntime.getTrendModule()
                .getTrendWsHub()
                .unregister(session != null ? session.getId() : null);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        AppRuntime.getTrendModule()
                .getTrendWsHub()
                .unregister(session != null ? session.getId() : null);
    }

    private String getQueryParam(Session session, String key) {
        String qs = session.getRequestURI() != null
                ? session.getRequestURI().getQuery()
                : null;

        if (qs == null || qs.length() == 0) {
            return null;
        }

        String[] pairs = qs.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = pair.substring(0, idx);
                String v = pair.substring(idx + 1);
                if (key.equals(k)) {
                    return v;
                }
            }
        }
        return null;
    }
}
