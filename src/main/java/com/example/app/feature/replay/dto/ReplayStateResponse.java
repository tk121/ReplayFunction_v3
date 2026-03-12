package com.example.app.feature.replay.dto;

/**
 * replay の現在状態を返すレスポンスDTOです。
 *
 * <p>
 * HTTP の状態取得APIや WebSocket 配信で利用します。
 * 操作画面全体の再生状態と、VDUごとの表示状態の両方を表現します。
 * </p>
 */
public class ReplayStateResponse {

    /** ルームID */
    private String roomId;

    /** 現在の操作者名 */
    private String operatorName;

    /** 現在の操作者IP */
    private String operatorIp;

    /** 再生状態（STOPPED / PLAYING） */
    private String playStatus;

    /** 再生開始日時 */
    private String startDateTime;

    /** 表示期間（時間） */
    private int periodHours;

    /** 現在の再生位置 */
    private String currentReplayTime;

    /** 再生速度 */
    private int speed;

    /** 最後に実行したコマンド */
    private String lastCommand;

    /**
     * 対象VDU番号です。
     *
     * <p>
     * 0 の場合は全体状態、1以上の場合は該当VDUの状態として扱います。
     * </p>
     */
    private int selectedVduNo;

    /**
     * 表示用URLです。
     *
     * <p>
     * OPENイベントで pageId から解決した URL を保持します。
     * vdu.html 側ではこの値を iframe の src に設定します。
     * </p>
     */
    private String displayUrl;

    /** 現在表示中の pageId */
    private String currentPageId;

    /** 最後に適用した event_id */
    private Long lastAppliedEventId;

    /** 最後に適用した event_type */
    private String lastAppliedEventType;

    /** 最後に適用した vdu_no */
    private Integer lastAppliedVduNo;

    /** 最後の適用結果（SUCCESS / FAIL） */
    private String lastApplyResult;

    /** 最後に適用したイベントの発生時刻 */
    private String lastAppliedOccurredAt;

    /** 最後に適用した control_id */
    private String lastControlId;

    /** 最後に適用した symbol_id */
    private String lastSymbolId;

    /** 最後に適用した value */
    private String lastValue;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorIp() {
        return operatorIp;
    }

    public void setOperatorIp(String operatorIp) {
        this.operatorIp = operatorIp;
    }

    public String getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(String playStatus) {
        this.playStatus = playStatus;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public int getPeriodHours() {
        return periodHours;
    }

    public void setPeriodHours(int periodHours) {
        this.periodHours = periodHours;
    }

    public String getCurrentReplayTime() {
        return currentReplayTime;
    }

    public void setCurrentReplayTime(String currentReplayTime) {
        this.currentReplayTime = currentReplayTime;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
    }

    public int getSelectedVduNo() {
        return selectedVduNo;
    }

    public void setSelectedVduNo(int selectedVduNo) {
        this.selectedVduNo = selectedVduNo;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public String getCurrentPageId() {
        return currentPageId;
    }

    public void setCurrentPageId(String currentPageId) {
        this.currentPageId = currentPageId;
    }

    public Long getLastAppliedEventId() {
        return lastAppliedEventId;
    }

    public void setLastAppliedEventId(Long lastAppliedEventId) {
        this.lastAppliedEventId = lastAppliedEventId;
    }

    public String getLastAppliedEventType() {
        return lastAppliedEventType;
    }

    public void setLastAppliedEventType(String lastAppliedEventType) {
        this.lastAppliedEventType = lastAppliedEventType;
    }

    public Integer getLastAppliedVduNo() {
        return lastAppliedVduNo;
    }

    public void setLastAppliedVduNo(Integer lastAppliedVduNo) {
        this.lastAppliedVduNo = lastAppliedVduNo;
    }

    public String getLastApplyResult() {
        return lastApplyResult;
    }

    public void setLastApplyResult(String lastApplyResult) {
        this.lastApplyResult = lastApplyResult;
    }

    public String getLastAppliedOccurredAt() {
        return lastAppliedOccurredAt;
    }

    public void setLastAppliedOccurredAt(String lastAppliedOccurredAt) {
        this.lastAppliedOccurredAt = lastAppliedOccurredAt;
    }

    public String getLastControlId() {
        return lastControlId;
    }

    public void setLastControlId(String lastControlId) {
        this.lastControlId = lastControlId;
    }

    public String getLastSymbolId() {
        return lastSymbolId;
    }

    public void setLastSymbolId(String lastSymbolId) {
        this.lastSymbolId = lastSymbolId;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }
}