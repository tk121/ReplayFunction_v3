package com.example.app.feature.replay.graphic.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.app.feature.replay.graphic.model.ReplayAvduAlert;

/**
 * replay の現在状態を返すレスポンスDTOです。
 *
 * <p>
 * HTTP の状態取得APIや WebSocket 配信で利用します。
 * 操作画面全体の再生状態と、VDUごとの表示状態の両方を表現します。
 * </p>
 */
public class ReplayStateResponse {

    /** EVENT / VDU / AVDU */
    private String clientType;

    /** ルームID */
    private String roomId;
    private Integer unitNo;
    private String replayMode;
    /** 現在の操作者IP */
    private String operatorIp;

    /** 再生状態（STOPPED / PLAYING） */
    private String playStatus;

    /** 再生開始日時 */
    private LocalDateTime startDateTime;
    private LocalDateTime currentReplayTime;

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
    private LocalDateTime lastAppliedOccurredAt;

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

    private boolean loggedIn;
    private String currentUserName;
    private boolean currentUserCanControl;

    /** 条件反映済みかどうか */
    private boolean conditionApplied;

    /** event 系列 */
    private Map<String, Map<String, Integer>> eventSeries = new LinkedHashMap<String, Map<String, Integer>>();

    /** AVDU 向けアラート一覧 */
    private List<ReplayAvduAlert> avduAlerts = new ArrayList<ReplayAvduAlert>();

    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Integer getUnitNo() { return unitNo; }
    public void setUnitNo(Integer unitNo) { this.unitNo = unitNo; }

    public String getReplayMode() { return replayMode; }
    public void setReplayMode(String replayMode) { this.replayMode = replayMode; }

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

    public int getSelectedVduNo() { return selectedVduNo; }
    public void setSelectedVduNo(int selectedVduNo) { this.selectedVduNo = selectedVduNo; }

    public String getLastPageId() { return lastPageId; }
    public void setLastPageId(String lastPageId) { this.lastPageId = lastPageId; }

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

    public boolean isCanOperate() { return canOperate; }
    public void setCanOperate(boolean canOperate) { this.canOperate = canOperate; }

    public String getControllerUserName() { return controllerUserName; }
    public void setControllerUserName(String controllerUserName) { this.controllerUserName = controllerUserName; }

    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }

    public String getCurrentUserName() { return currentUserName; }
    public void setCurrentUserName(String currentUserName) { this.currentUserName = currentUserName; }

    public boolean isCurrentUserCanControl() { return currentUserCanControl; }
    public void setCurrentUserCanControl(boolean currentUserCanControl) { this.currentUserCanControl = currentUserCanControl; }

    public boolean isConditionApplied() { return conditionApplied; }
    public void setConditionApplied(boolean conditionApplied) { this.conditionApplied = conditionApplied; }

    public Map<String, Map<String, Integer>> getEventSeries() { return eventSeries; }
    public void setEventSeries(Map<String, Map<String, Integer>> eventSeries) { this.eventSeries = eventSeries; }

    public List<ReplayAvduAlert> getAvduAlerts() { return avduAlerts; }
    public void setAvduAlerts(List<ReplayAvduAlert> avduAlerts) { this.avduAlerts = avduAlerts; }
}