package com.example.app.support.debug.dto;

import java.io.Serializable;

public class DebugReplayStateDto implements Serializable {

    private String replayStatus;
    private String currentReplayTime;
    private String baseStartTime;
    private String speed;
    private String targetFrom;
    private String targetTo;
    private String operatorUserId;
    private int connectedSessionCount;
    private String lastTickTime;
    private long lastTickDurationMs;

    public String getReplayStatus() {
        return replayStatus;
    }

    public void setReplayStatus(String replayStatus) {
        this.replayStatus = replayStatus;
    }

    public String getCurrentReplayTime() {
        return currentReplayTime;
    }

    public void setCurrentReplayTime(String currentReplayTime) {
        this.currentReplayTime = currentReplayTime;
    }

    public String getBaseStartTime() {
        return baseStartTime;
    }

    public void setBaseStartTime(String baseStartTime) {
        this.baseStartTime = baseStartTime;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getTargetFrom() {
        return targetFrom;
    }

    public void setTargetFrom(String targetFrom) {
        this.targetFrom = targetFrom;
    }

    public String getTargetTo() {
        return targetTo;
    }

    public void setTargetTo(String targetTo) {
        this.targetTo = targetTo;
    }

    public String getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(String operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public int getConnectedSessionCount() {
        return connectedSessionCount;
    }

    public void setConnectedSessionCount(int connectedSessionCount) {
        this.connectedSessionCount = connectedSessionCount;
    }

    public String getLastTickTime() {
        return lastTickTime;
    }

    public void setLastTickTime(String lastTickTime) {
        this.lastTickTime = lastTickTime;
    }

    public long getLastTickDurationMs() {
        return lastTickDurationMs;
    }

    public void setLastTickDurationMs(long lastTickDurationMs) {
        this.lastTickDurationMs = lastTickDurationMs;
    }
}
