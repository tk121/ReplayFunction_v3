package com.example.app.feature.auth.session;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LoginUserRegistry {

    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    public synchronized boolean tryLogin(String userId, String sessionId, int max) {

        logoutBySessionId(sessionId);

        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.add(sessionId);
            sessionToUser.put(sessionId, userId);
            return true;
        }

        if (userSessions.size() >= max) {
            return false;
        }

        Set<String> set = ConcurrentHashMap.newKeySet();
        set.add(sessionId);

        userSessions.put(userId, set);
        sessionToUser.put(sessionId, userId);

        return true;
    }

    public synchronized void logoutBySessionId(String sessionId) {
        String userId = sessionToUser.remove(sessionId);
        if (userId == null) return;

        Set<String> set = userSessions.get(userId);
        if (set != null) {
            set.remove(sessionId);
            if (set.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public synchronized int getLoginUserCount() {
        return userSessions.size();
    }

    public synchronized boolean isLoggedIn(String userId) {
        Set<String> set = userSessions.get(userId);
        return set != null && !set.isEmpty();
    }
}
