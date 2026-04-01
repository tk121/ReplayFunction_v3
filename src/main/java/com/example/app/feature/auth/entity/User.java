package com.example.app.feature.auth.entity;

public class User {

    private Long userId;
    private String userName;
    private String password;
    private boolean canControl;
    private boolean enabled;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCanControl() {
        return canControl;
    }

    public void setCanControl(boolean canControl) {
        this.canControl = canControl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
