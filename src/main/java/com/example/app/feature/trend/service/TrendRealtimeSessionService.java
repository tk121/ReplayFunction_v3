package com.example.app.feature.trend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.app.feature.trend.model.TrendSubscription;

public class TrendRealtimeSessionService {

    private final Map<String, TrendSubscription> subscriptionMap =
            new ConcurrentHashMap<String, TrendSubscription>();

    public TrendSubscription subscribe(
            String userId,
            String clientId,
            String wsSessionId,
            Long trendId,
            LocalDateTime initialLastDeliveredTime) {

        String key = buildKey(userId, clientId);

        TrendSubscription subscription = new TrendSubscription();
        subscription.setUserId(userId);
        subscription.setClientId(clientId);
        subscription.setWsSessionId(wsSessionId);
        subscription.setTrendId(trendId);
        subscription.setRealtime(true);
        subscription.setLastDeliveredTime(initialLastDeliveredTime);

        subscriptionMap.put(key, subscription);
        return subscription;
    }

    public void unsubscribe(String userId, String clientId) {
        subscriptionMap.remove(buildKey(userId, clientId));
    }

    public void unsubscribeByWsSessionId(String wsSessionId) {
        if (wsSessionId == null) {
            return;
        }

        List<String> removeKeys = new ArrayList<String>();
        for (Map.Entry<String, TrendSubscription> entry : subscriptionMap.entrySet()) {
            TrendSubscription subscription = entry.getValue();
            if (subscription != null && wsSessionId.equals(subscription.getWsSessionId())) {
                removeKeys.add(entry.getKey());
            }
        }

        for (String key : removeKeys) {
            subscriptionMap.remove(key);
        }
    }

    public List<TrendSubscription> findAllRealtimeSubscriptions() {
        return new ArrayList<TrendSubscription>(subscriptionMap.values());
    }

    private String buildKey(String userId, String clientId) {
        return String.valueOf(userId) + ":" + String.valueOf(clientId);
    }
}