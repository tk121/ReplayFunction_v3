package com.example.app.feature.trend.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.trend.dto.TrendDeltaResponse;
import com.example.app.feature.trend.model.TrendSubscription;

public class TrendWsHub {

    private final Map<String, TrendWsClient> clientMap =
            new ConcurrentHashMap<String, TrendWsClient>();

    public void register(TrendWsClient client) {
        clientMap.put(client.getSession().getId(), client);
    }

    public void unregister(String wsSessionId) {
        if (wsSessionId != null) {
            clientMap.remove(wsSessionId);
            AppRuntime.getTrendModule()
                    .getTrendRealtimeSessionService()
                    .unsubscribeByWsSessionId(wsSessionId);
        }
    }

    public void bindWsSessionId(String userId, String clientId, String wsSessionId) {
        for (TrendSubscription subscription : AppRuntime.getTrendModule()
                .getTrendRealtimeSessionService()
                .findAllRealtimeSubscriptions()) {

            if (subscription == null) {
                continue;
            }
            if (safeEquals(userId, subscription.getUserId())
                    && safeEquals(clientId, subscription.getClientId())) {
                subscription.setWsSessionId(wsSessionId);
            }
        }
    }

    public void sendDelta(TrendSubscription subscription, TrendDeltaResponse response) throws Exception {
        if (subscription == null || subscription.getWsSessionId() == null) {
            return;
        }

        TrendWsClient client = clientMap.get(subscription.getWsSessionId());
        if (client == null || client.getSession() == null || !client.getSession().isOpen()) {
            return;
        }

        client.getSession().getBasicRemote().sendText(JsonUtil.writeValueAsString(response));
    }

    private boolean safeEquals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}