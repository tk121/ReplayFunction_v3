package com.example.app.support.debug.dto;

import java.io.Serializable;

public class DebugCommandRequestDto implements Serializable {

    private String commandType;
    private String replayTime;
    private String speed;
    private String targetFrom;
    private String targetTo;

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getReplayTime() {
        return replayTime;
    }

    public void setReplayTime(String replayTime) {
        this.replayTime = replayTime;
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
}
