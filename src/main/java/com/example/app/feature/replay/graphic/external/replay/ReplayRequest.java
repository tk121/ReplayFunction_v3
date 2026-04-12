package com.example.app.feature.replay.graphic.external.replay;

/**
 * Replay 系 C プロセス向けの送信DTO例です。
 */
public class ReplayRequest {

    private String requestId;
    private String command;
    private String roomId;
    private String replayTime;

    public ReplayRequest() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getReplayTime() {
        return replayTime;
    }

    public void setReplayTime(String replayTime) {
        this.replayTime = replayTime;
    }
}