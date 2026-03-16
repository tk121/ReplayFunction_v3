package com.example.app.feature.replay.dto;

/**
 * heartbeat API 用のリクエストDTOです。
 *
 * <p>
 * 操作画面から一定間隔で送信し、
 * 現在の操作権保持者がまだ生きていることをサーバへ通知します。
 * </p>
 */
public class ReplayHeartbeatRequest {

    /** ルームID */
    private String roomId;

    /** クライアント識別ID */
    private String clientId;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}