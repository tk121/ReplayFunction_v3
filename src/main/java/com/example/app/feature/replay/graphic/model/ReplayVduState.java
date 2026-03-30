package com.example.app.feature.replay.graphic.model;

import java.time.LocalDateTime;

/**
 * VDUごとの再生状態を保持するクラスです。
 *
 * <p>
 * replay 全体状態とは別に、
 * どの VDU がどの pageId を表示しているか、
 * 最後にどのイベントが適用されたかを保持します。
 * </p>
 */
public class ReplayVduState {

    /** VDU番号 */
    private int vduNo;

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

    /** 最後に適用したイベント発生時刻 */
    private LocalDateTime lastAppliedOccurredAt;

    /** 最後に適用した control_id */
    private String lastControlId;

    /** 最後に適用した symbol_id */
    private String lastSymbolId;

    /** 最後に適用した value */
    private String lastValue;

    public int getVduNo() {
        return vduNo;
    }

    public void setVduNo(int vduNo) {
        this.vduNo = vduNo;
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

    public LocalDateTime getLastAppliedOccurredAt() {
        return lastAppliedOccurredAt;
    }

    public void setLastAppliedOccurredAt(LocalDateTime lastAppliedOccurredAt) {
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

    /**
     * イベント適用結果に関する項目だけをクリアします。
     *
     * <p>
     * 現在表示中の pageId や displayUrl は維持しつつ、
     * 「最後に何を適用したか」の情報だけ初期化したい場合に使用します。
     * </p>
     */
    public void clearEventStatusOnly() {
        this.lastAppliedOperationId = null;
        this.lastAppliedActionType = null;
        this.lastAppliedVduNo = null;
        this.lastApplyResult = null;
        this.lastAppliedOccurredAt = null;
        this.lastControlId = null;
        this.lastSymbolId = null;
        this.lastValue = null;
    }
}