package com.example.app.feature.replay.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * replay 全体の状態を保持するクラスです。
 *
 * <p>
 * 1つの room に対して1つ存在し、
 * 再生状態・再生条件・最後に適用したイベント情報・
 * VDUごとの状態をまとめて保持します。
 * </p>
 */
public class ReplayState {

    /** 停止中状態 */
    public static final String STATUS_STOPPED = "STOPPED";

    /** 再生中状態 */
    public static final String STATUS_PLAYING = "PLAYING";

    /** ルームID */
    private String roomId;

    /** 操作者名 */
    private String operatorName;

    /** 操作者IP */
    private String operatorIp;

    /** 再生状態 */
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

    /** 最後に適用した event_id */
    private Long lastAppliedEventId;

    /** 最後に適用した event_type */
    private String lastAppliedEventType;

    /** 最後に適用した vdu_no */
    private Integer lastAppliedVduNo;

    /** 最後の適用結果 */
    private String lastApplyResult;

    /** 最後に適用したイベント発生時刻 */
    private String lastAppliedOccurredAt;

    /** 最後に適用した control_id */
    private String lastControlId;

    /** 最後に適用した symbol_id */
    private String lastSymbolId;

    /** 最後に適用した value */
    private String lastValue;

    /**
     * VDUごとの状態保持マップです。
     *
     * <p>
     * key は vduNo、value はその VDU の現在状態です。
     * </p>
     */
    private final Map<Integer, ReplayVduState> vduStateMap = new ConcurrentHashMap<Integer, ReplayVduState>();

    public ReplayState() {
        this.playStatus = STATUS_STOPPED;
        this.periodHours = 4;
        this.speed = 1;
        this.lastCommand = "INIT";
    }

    /**
     * 指定した VDU の状態を取得します。
     *
     * <p>
     * 未作成であれば新しく生成して保持してから返します。
     * </p>
     *
     * @param vduNo VDU番号
     * @return VDUごとの状態
     */
    public ReplayVduState getOrCreateVduState(int vduNo) {
        ReplayVduState current = vduStateMap.get(Integer.valueOf(vduNo));
        if (current != null) {
            return current;
        }

        ReplayVduState created = new ReplayVduState();
        created.setVduNo(vduNo);

        ReplayVduState old = vduStateMap.putIfAbsent(Integer.valueOf(vduNo), created);
        return old != null ? old : created;
    }

    public Map<Integer, ReplayVduState> getVduStateMap() {
        return vduStateMap;
    }

    /**
     * 全VDUについて、イベント適用結果だけをクリアします。
     *
     * <p>
     * pageId や displayUrl は維持し、
     * 最後に適用したイベント情報だけ初期化します。
     * </p>
     */
    public void clearAllVduEventStatusOnly() {
        for (ReplayVduState vduState : vduStateMap.values()) {
            vduState.clearEventStatusOnly();
        }
    }

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