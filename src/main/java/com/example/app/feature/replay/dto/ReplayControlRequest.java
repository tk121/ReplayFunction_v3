package com.example.app.feature.replay.dto;

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
     * 現状は global 1つのみを想定していますが、
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
    private String startDateTime;

    /**
     * 表示期間（時間）です。
     *
     * <p>
     * 現在は 4 / 12 / 24 のいずれかを想定しています。
     * </p>
     */
    private Integer periodHours;

    /**
     * 操作者名です。
     *
     * <p>
     * 誰が操作しているかを画面へ表示するために保持します。
     * </p>
     */
    private String operatorName;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Integer getPeriodHours() {
        return periodHours;
    }

    public void setPeriodHours(Integer periodHours) {
        this.periodHours = periodHours;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}