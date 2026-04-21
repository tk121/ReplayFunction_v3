package com.example.app.feature.auth.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUserRegistry {

    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
    private final long timeout;

    public OnlineUserRegistry(long timeout) {
        this.timeout = timeout;
    }

    public void touch(String userId) {
        lastSeen.put(userId, System.currentTimeMillis());
    }

    public void remove(String userId) {
        lastSeen.remove(userId);
    }

    public int count() {
        cleanup();
        return lastSeen.size();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        List<String> remove = new ArrayList<>();

        for (Map.Entry<String, Long> e : lastSeen.entrySet()) {
            if (now - e.getValue() > timeout) {
                remove.add(e.getKey());
            }
        }
        remove.forEach(lastSeen::remove);
    }
}