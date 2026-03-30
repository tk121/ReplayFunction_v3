package com.example.app.feature.replay.common.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.app.feature.replay.graphic.model.ReplayAvduState;
import com.example.app.feature.replay.graphic.model.ReplayVduState;

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
    private Integer unitNo;
    private ReplayMode replayMode = ReplayMode.HISTORY;

	/** 現在の操作者名 */
	private String operatorName;

	/** 現在の操作者IP */
	private String operatorIp;
    private String playStatus = STATUS_STOPPED;

    /** HISTORY での共有基準開始日時。REALTIME では当日 00:00:00 を保持 */
	private LocalDateTime startDateTime;
    /** 全ユーザー共通の再生時刻 */
	private LocalDateTime currentReplayTime;

    private int speed = 1;
    private String lastCommand = "INIT";

	private Long lastAppliedOperationId;
	private String lastAppliedActionType;
	private Integer lastAppliedVduNo;
	private String lastApplyResult;
	private LocalDateTime lastAppliedOccurredAt;
	private String lastControlId;
	private String lastButtonId;
	private String lastValue;

    /** ログインで奪取される後勝ちの操作者 */
    private String controllerUserId;

	/**
	 * 現在の操作権保持者名です。
	 *
	 * <p>
	 * 画面表示用に保持します。
	 * </p>
	 */
	private String controllerUserName;

	private final Map<Integer, ReplayVduState> vduStateMap = new ConcurrentHashMap<Integer, ReplayVduState>();
	
    /** AVDU の現在表示状態 */
    private ReplayAvduState avduState = new ReplayAvduState();

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

	public void clearAllVduEventStatusOnly() {
		for (ReplayVduState vduState : vduStateMap.values()) {
			vduState.clearEventStatusOnly();
		}
	}
	
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public Integer getUnitNo() { return unitNo; }
    public void setUnitNo(Integer unitNo) { this.unitNo = unitNo; }
    public ReplayMode getReplayMode() { return replayMode; }
    public void setReplayMode(ReplayMode replayMode) { this.replayMode = replayMode; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getOperatorIp() { return operatorIp; }
    public void setOperatorIp(String operatorIp) { this.operatorIp = operatorIp; }
    public String getPlayStatus() { return playStatus; }
    public void setPlayStatus(String playStatus) { this.playStatus = playStatus; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public LocalDateTime getCurrentReplayTime() { return currentReplayTime; }
    public void setCurrentReplayTime(LocalDateTime currentReplayTime) { this.currentReplayTime = currentReplayTime; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public String getLastCommand() { return lastCommand; }
    public void setLastCommand(String lastCommand) { this.lastCommand = lastCommand; }
    public Long getLastAppliedOperationId() { return lastAppliedOperationId; }
    public void setLastAppliedOperationId(Long lastAppliedOperationId) { this.lastAppliedOperationId = lastAppliedOperationId; }
    public String getLastAppliedActionType() { return lastAppliedActionType; }
    public void setLastAppliedActionType(String lastAppliedActionType) { this.lastAppliedActionType = lastAppliedActionType; }
    public Integer getLastAppliedVduNo() { return lastAppliedVduNo; }
    public void setLastAppliedVduNo(Integer lastAppliedVduNo) { this.lastAppliedVduNo = lastAppliedVduNo; }
    public String getLastApplyResult() { return lastApplyResult; }
    public void setLastApplyResult(String lastApplyResult) { this.lastApplyResult = lastApplyResult; }
    public LocalDateTime getLastAppliedOccurredAt() { return lastAppliedOccurredAt; }
    public void setLastAppliedOccurredAt(LocalDateTime lastAppliedOccurredAt) { this.lastAppliedOccurredAt = lastAppliedOccurredAt; }
    public String getLastControlId() { return lastControlId; }
    public void setLastControlId(String lastControlId) { this.lastControlId = lastControlId; }
    public String getLastButtonId() { return lastButtonId; }
    public void setLastButtonId(String lastButtonId) { this.lastButtonId = lastButtonId; }
    public String getLastValue() { return lastValue; }
    public void setLastValue(String lastValue) { this.lastValue = lastValue; }
    public String getControllerUserId() { return controllerUserId; }
    public void setControllerUserId(String controllerUserId) { this.controllerUserId = controllerUserId; }
    public String getControllerUserName() { return controllerUserName; }
    public void setControllerUserName(String controllerUserName) { this.controllerUserName = controllerUserName; }
    public Map<Integer, ReplayVduState> getVduStateMap() { return vduStateMap; }
    public ReplayAvduState getAvduState() { return avduState; }
    public void setAvduState(ReplayAvduState avduState) { this.avduState = avduState; }
}
