package com.example.app.support.debug.dto;

import java.io.Serializable;

public class DebugHealthDto implements Serializable {

    private String appStatus;
    private String databaseStatus;
    private String replayEngineStatus;
    private String wsHubStatus;
    private String message;

    public String getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(String appStatus) {
        this.appStatus = appStatus;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public String getReplayEngineStatus() {
        return replayEngineStatus;
    }

    public void setReplayEngineStatus(String replayEngineStatus) {
        this.replayEngineStatus = replayEngineStatus;
    }

    public String getWsHubStatus() {
        return wsHubStatus;
    }

    public void setWsHubStatus(String wsHubStatus) {
        this.wsHubStatus = wsHubStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
