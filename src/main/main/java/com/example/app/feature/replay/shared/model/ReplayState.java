package com.example.app.feature.replay.shared.model;

import java.time.LocalDateTime;

/**
 * 共有リプレイ状態の最小ひな形です。
 * 実プロジェクト側の ReplayState がある場合はそちらに寄せてください。
 */
public class ReplayState {

    private boolean playing;
    private LocalDateTime currentReplayTime;
    private LocalDateTime baseStartTime;
    private LocalDateTime targetFrom;
    private LocalDateTime targetTo;
    private int speed;
    private String operatorUserId;
    private LocalDateTime lastTickTime;
    private long lastTickDurationMs;

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public LocalDateTime getCurrentReplayTime() {
        return currentReplayTime;
    }

    public void setCurrentReplayTime(LocalDateTime currentReplayTime) {
        this.currentReplayTime = currentReplayTime;
    }

    public LocalDateTime getBaseStartTime() {
        return baseStartTime;
    }

    public void setBaseStartTime(LocalDateTime baseStartTime) {
        this.baseStartTime = baseStartTime;
    }

    public LocalDateTime getTargetFrom() {
        return targetFrom;
    }

    public void setTargetFrom(LocalDateTime targetFrom) {
        this.targetFrom = targetFrom;
    }

    public LocalDateTime getTargetTo() {
        return targetTo;
    }

    public void setTargetTo(LocalDateTime targetTo) {
        this.targetTo = targetTo;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(String operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public LocalDateTime getLastTickTime() {
        return lastTickTime;
    }

    public void setLastTickTime(LocalDateTime lastTickTime) {
        this.lastTickTime = lastTickTime;
    }

    public long getLastTickDurationMs() {
        return lastTickDurationMs;
    }

    public void setLastTickDurationMs(long lastTickDurationMs) {
        this.lastTickDurationMs = lastTickDurationMs;
    }
}
