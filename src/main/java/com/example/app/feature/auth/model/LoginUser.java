package com.example.app.feature.auth.model;

public class LoginUser {
    private final String userId;
    private final String userName;
    private final boolean canControl;

    public LoginUser(String userId, String userName, boolean canControl) {
        this.userId = userId;
        this.userName = userName;
        this.canControl = canControl;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public boolean isCanControl() { return canControl; }
}
