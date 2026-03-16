package com.example.app.feature.replay.service;

/**
 * replay の操作権制御設定です。
 *
 * <p>
 * heartbeat を有効にするかどうか、
 * 有効な場合のタイムアウト秒数を保持します。
 * </p>
 */
public class ReplayControlConfig {

    /** heartbeat を有効にするか */
    private boolean heartbeatEnabled;

    /** heartbeat タイムアウト秒数 */
    private int heartbeatTimeoutSeconds;

    public ReplayControlConfig() {
        this(true, 30);
    }

    public ReplayControlConfig(boolean heartbeatEnabled, int heartbeatTimeoutSeconds) {
        this.heartbeatEnabled = heartbeatEnabled;
        this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
    }

    public boolean isHeartbeatEnabled() {
        return heartbeatEnabled;
    }

    public void setHeartbeatEnabled(boolean heartbeatEnabled) {
        this.heartbeatEnabled = heartbeatEnabled;
    }

    public int getHeartbeatTimeoutSeconds() {
        return heartbeatTimeoutSeconds;
    }

    public void setHeartbeatTimeoutSeconds(int heartbeatTimeoutSeconds) {
        this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
    }
}