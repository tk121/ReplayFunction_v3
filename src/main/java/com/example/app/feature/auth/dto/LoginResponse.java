package com.example.app.feature.auth.dto;

public class LoginResponse {
    private String userId;
    private String userName;
    private boolean canControl;
    private boolean loggedIn;
    private String controllerUserName;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public boolean isCanControl() { return canControl; }
    public void setCanControl(boolean canControl) { this.canControl = canControl; }
    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
    public String getControllerUserName() { return controllerUserName; }
    public void setControllerUserName(String controllerUserName) { this.controllerUserName = controllerUserName; }
}
