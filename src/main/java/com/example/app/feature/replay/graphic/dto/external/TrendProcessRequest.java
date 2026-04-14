package com.example.app.feature.replay.graphic.dto.external;

public class TrendProcessRequest {

    private String command;
    private String replayTime;

    public TrendProcessRequest() {
    }

    public TrendProcessRequest(String command, String replayTime) {
        this.command = command;
        this.replayTime = replayTime;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getReplayTime() {
        return replayTime;
    }

    public void setReplayTime(String replayTime) {
        this.replayTime = replayTime;
    }
}