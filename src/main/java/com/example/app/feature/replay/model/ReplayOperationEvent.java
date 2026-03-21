package com.example.app.feature.replay.model;

import java.time.LocalDateTime;

/**
 * replay 用の操作イベントモデル
 *
 * <p>
 * OperationLog(entity) を replay 処理用に変換したもの。
 * DB構造から独立させることで、業務ロジックを安定させる。
 * </p>
 */
public class ReplayOperationEvent {

    private long operationId;
    private int vduNo;
    private String actionType;
    private String pageId;
    private String controlId;
    private String buttonId;
    private String value;
    private LocalDateTime occurredAt;

    // --- getter/setter ---

    public long getOperationId() {
        return operationId;
    }

    public void setOperationId(long operationId) {
        this.operationId = operationId;
    }

    public int getVduNo() {
        return vduNo;
    }

    public void setVduNo(int vduNo) {
        this.vduNo = vduNo;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getControlId() {
        return controlId;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }

    public String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}