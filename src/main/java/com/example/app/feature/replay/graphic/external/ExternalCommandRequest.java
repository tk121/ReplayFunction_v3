package com.example.app.feature.replay.graphic.external;

/**
 * C プロセスへ渡す入力データです。
 *
 * <p>
 * event_log の1イベントを C 側へ適用させるための JSON 変換元オブジェクトです。
 * </p>
 */
public class ExternalCommandRequest {

    /** イベント種別（OPEN / CLICK / INPUT） */
    private String eventType;

    /** 対象 page_id */
    private String pageId;

    /** CLICK 時の対象 control_id */
    private String controlId;

    /** INPUT 時の対象 symbol_id */
    private String symbolId;

    /** INPUT 時の入力値 */
    private String value;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}