package com.example.app.feature.auth.session;

public class LoginPolicy {

    private final int maxLoginUsers;
    private final long onlineTimeoutMillis;

    public LoginPolicy(int maxLoginUsers, long onlineTimeoutMillis) {
        this.maxLoginUsers = maxLoginUsers;
        this.onlineTimeoutMillis = onlineTimeoutMillis;
    }

    public int getMaxLoginUsers() {
        return maxLoginUsers;
    }

    public long getOnlineTimeoutMillis() {
        return onlineTimeoutMillis;
    }
}
