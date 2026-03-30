package com.example.app.feature.replay.graphic.dto;

import java.time.LocalDateTime;

/**
 * replay 制御APIで受け取るリクエストDTOです。
 *
 * <p>
 * 操作画面から送られてくる再生条件やコマンドを保持します。
 * 例:
 * </p>
 * <ul>
 *   <li>再生開始</li>
 *   <li>停止</li>
 *   <li>早送り</li>
 *   <li>先頭へ移動</li>
 *   <li>末尾へ移動</li>
 *   <li>条件適用</li>
 * </ul>
 */
public class ReplayControlRequest {

    /**
     * ルームIDです。
     *
     * <p>
     * 現状は replayMode 1つのみを想定していますが、
     * 将来的に複数ルーム対応する場合に備えて保持します。
     * </p>
     */
    private String roomId;

    /**
     * 実行するコマンドです。
     *
     * <p>
     * 例:
     * </p>
     * <ul>
     *   <li>APPLY_CONDITION</li>
     *   <li>PLAY</li>
     *   <li>STOP</li>
     *   <li>FAST_FORWARD</li>
     *   <li>GO_HEAD</li>
     *   <li>GO_TAIL</li>
     * </ul>
     */
    private String command;

    /**
     * 再生開始日時です。
     *
     * <p>
     * 文字列形式で保持し、Service 層で LocalDateTime に変換して扱います。
     * </p>
     */
    private LocalDateTime startDateTime;
    private Integer displayHours;
    private Integer unitNo;
    private String replayMode;
    private String operatorName;
    private String clientId;

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public Integer getDisplayHours() { return displayHours; }
    public void setDisplayHours(Integer displayHours) { this.displayHours = displayHours; }
    public Integer getUnitNo() { return unitNo; }
    public void setUnitNo(Integer unitNo) { this.unitNo = unitNo; }
    public String getReplayMode() { return replayMode; }
    public void setReplayMode(String replayMode) { this.replayMode = replayMode; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
}
