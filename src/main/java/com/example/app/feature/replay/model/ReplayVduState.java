package com.example.app.feature.replay.model;

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

    /** 現在表示中の pageId */
    private String currentPageId;

    /** iframe に設定する表示URL */
    private String displayUrl;

    /** 最後に適用した event_id */
    private Long lastAppliedEventId;

    /** 最後に適用した event_type */
    private String lastAppliedEventType;

    /** 最後に適用した vdu_no */
    private Integer lastAppliedVduNo;

    /** 最後の適用結果（SUCCESS / FAIL） */
    private String lastApplyResult;

    /** 最後に適用したイベント発生時刻 */
    private String lastAppliedOccurredAt;

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

    public String getCurrentPageId() {
        return currentPageId;
    }

    public void setCurrentPageId(String currentPageId) {
        this.currentPageId = currentPageId;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
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

    /**
     * イベント適用結果に関する項目だけをクリアします。
     *
     * <p>
     * 現在表示中の pageId や displayUrl は維持しつつ、
     * 「最後に何を適用したか」の情報だけ初期化したい場合に使用します。
     * </p>
     */
    public void clearEventStatusOnly() {
        this.lastAppliedEventId = null;
        this.lastAppliedEventType = null;
        this.lastAppliedVduNo = null;
        this.lastApplyResult = null;
        this.lastAppliedOccurredAt = null;
        this.lastControlId = null;
        this.lastSymbolId = null;
        this.lastValue = null;
    }
}