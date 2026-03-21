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

	/** 現在の操作者名 */
	private String operatorName;

	/** 現在の操作者IP */
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

	/** 最後に適用した operation_id */
	private Long lastAppliedOperationId;

	/** 最後に適用した action_type */
	private String lastAppliedActionType;

	/** 最後に適用した vdu_no */
	private Integer lastAppliedVduNo;

	/** 最後の適用結果 */
	private String lastApplyResult;

	/** 最後に適用したイベント発生時刻 */
	private String lastAppliedOccurredAt;

	/** 最後に適用した control_id */
	private String lastControlId;

	/** 最後に適用した symbol_id */
	private String lastButtonId;

	/** 最後に適用した value */
	private String lastValue;

	/**
	 * 現在の操作権保持クライアントIDです。
	 *
	 * <p>
	 * 操作権判定の本体はこの値で行います。
	 * </p>
	 */
	private String controllerClientId;

	/**
	 * 現在の操作権保持者名です。
	 *
	 * <p>
	 * 画面表示用に保持します。
	 * </p>
	 */
	private String controllerUserName;

	/** 最終 heartbeat 受信時刻（epoch millis） */
	private long lastHeartbeatTime;

	/** ロック取得時刻（epoch millis） */
	private long lockAcquiredTime;

	/**
	 * VDUごとの状態保持マップです。
	 *
	 * <p>
	 * key は vduNo、value はその VDU の現在状態です。
	 * </p>
	 */
	private final Map<Integer, ReplayVduState> vduStateMap = new ConcurrentHashMap<Integer, ReplayVduState>();
	
    /** AVDU の現在表示状態 */
    private ReplayAvduState avduState = new ReplayAvduState();

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
	
    public ReplayAvduState getAvduState() {
        return avduState;
    }

    public void setAvduState(ReplayAvduState avduState) {
        this.avduState = avduState;
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

	public Long getLastAppliedOperationId() {
		return lastAppliedOperationId;
	}

	public void setLastAppliedOperationId(Long lastAppliedOperationId) {
		this.lastAppliedOperationId = lastAppliedOperationId;
	}

	public String getLastAppliedActionType() {
		return lastAppliedActionType;
	}

	public void setLastAppliedActionType(String lastAppliedActionType) {
		this.lastAppliedActionType = lastAppliedActionType;
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

	public String getLastButtonId() {
		return lastButtonId;
	}

	public void setLastButtonId(String lastButtonId) {
		this.lastButtonId = lastButtonId;
	}

	public String getLastValue() {
		return lastValue;
	}

	public void setLastValue(String lastValue) {
		this.lastValue = lastValue;
	}

	public String getControllerClientId() {
		return controllerClientId;
	}

	public void setControllerClientId(String controllerClientId) {
		this.controllerClientId = controllerClientId;
	}

	public String getControllerUserName() {
		return controllerUserName;
	}

	public void setControllerUserName(String controllerUserName) {
		this.controllerUserName = controllerUserName;
	}

	public long getLastHeartbeatTime() {
		return lastHeartbeatTime;
	}

	public void setLastHeartbeatTime(long lastHeartbeatTime) {
		this.lastHeartbeatTime = lastHeartbeatTime;
	}

	public long getLockAcquiredTime() {
		return lockAcquiredTime;
	}

	public void setLockAcquiredTime(long lockAcquiredTime) {
		this.lockAcquiredTime = lockAcquiredTime;
	}

	public static String getStatusStopped() {
		return STATUS_STOPPED;
	}

	public static String getStatusPlaying() {
		return STATUS_PLAYING;
	}

}