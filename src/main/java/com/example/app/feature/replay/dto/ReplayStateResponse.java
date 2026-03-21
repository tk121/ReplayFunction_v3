package com.example.app.feature.replay.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.app.feature.replay.model.ReplayAvduAlert;

/**
 * replay の現在状態を返すレスポンスDTOです。
 *
 * <p>
 * HTTP の状態取得APIや WebSocket 配信で利用します。
 * 操作画面全体の再生状態と、VDUごとの表示状態の両方を表現します。
 * </p>
 */
public class ReplayStateResponse {
	
    /** CONTROL / VDU / AVDU */
    private String clientType;

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

    /** 最後に適用した pageId */
    private String lastPageId;

    /** 最後に適用した operation_id */
    private Long lastAppliedOperationId;

    /** 最後に適用した action_type */
    private String lastAppliedActionType;

    /** 最後に適用した vdu_no */
    private Integer lastAppliedVduNo;

    /** 最後の適用結果（SUCCESS / FAIL） */
    private String lastApplyResult;

    /** 最後に適用したイベントの発生時刻 */
    private String lastAppliedOccurredAt;

    /** 最後に適用した control_id */
    private String lastControlId;

    /** 最後に適用した symbol_id */
    private String lastButtonId;

    /** 最後に適用した value */
    private String lastValue;
    
    /**
     * このレスポンスを受け取ったクライアントが操作可能かどうかです。
     *
     * <p>
     * 操作画面ではこの値を見てボタンをグレーアウトします。
     * </p>
     */
    private boolean canOperate;

    /** 現在の操作権保持者名 */
    private String controllerUserName;


    public String getRoomId() {
        return roomId;
    }
    
    /** AVDU 向けアラート一覧 */
    private List<ReplayAvduAlert> avduAlerts = new ArrayList<ReplayAvduAlert>();


	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
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

	public String getLastPageId() {
		return lastPageId;
	}

	public void setLastPageId(String lastPageId) {
		this.lastPageId = lastPageId;
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

	public boolean isCanOperate() {
		return canOperate;
	}

	public void setCanOperate(boolean canOperate) {
		this.canOperate = canOperate;
	}

	public String getControllerUserName() {
		return controllerUserName;
	}

	public void setControllerUserName(String controllerUserName) {
		this.controllerUserName = controllerUserName;
	}

	public List<ReplayAvduAlert> getAvduAlerts() {
		return avduAlerts;
	}

	public void setAvduAlerts(List<ReplayAvduAlert> avduAlerts) {
		this.avduAlerts = avduAlerts;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

 
}