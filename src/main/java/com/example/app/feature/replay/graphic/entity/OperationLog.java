package com.example.app.feature.replay.graphic.entity;

import java.time.LocalDateTime;

/**
 * event_log テーブルの1レコードを表すエンティティです。
 *
 * <p>
 * DB に保存された操作履歴を Java 上で扱うためのクラスです。
 * replay ではこの情報を時系列順に読み出し、
 * C プロセスへ渡して画面再現を行います。
 * </p>
 */
public class OperationLog {

    /** operation_id: 主キー */
    private long operationId;

    /** unit_no: ユニット番号 */
    private Integer unitNo;

    /** graphic_type: VDU / AVDU / LDP など */
    private String graphicType;

    /** vdu_no: 対象VDU番号 */
    private int vduNo;

    /** occurred_at: 操作発生時刻 */
    private LocalDateTime occurredAt;

    /** action_type: OPEN / CLICK / INPUT */
    private String actionType;

    /** page_id: 遷移先または対象画面 */
    private String pageId;

    /** control_id: CLICK の対象コントロール */
    private String controlId;

    /** symbol_id: INPUT の対象シンボル */
    private String buttonId;

    /** value: INPUT の入力値 */
    private String value;

	public long getOperationId() {
		return operationId;
	}

	public void setOperationId(long operationId) {
		this.operationId = operationId;
	}

	public Integer getUnitNo() {
		return unitNo;
	}

	public void setUnitNo(Integer unitNo) {
		this.unitNo = unitNo;
	}

	public String getGraphicType() {
		return graphicType;
	}

	public void setGraphicType(String graphicType) {
		this.graphicType = graphicType;
	}

	public int getVduNo() {
		return vduNo;
	}

	public void setVduNo(int vduNo) {
		this.vduNo = vduNo;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(LocalDateTime occurredAt) {
		this.occurredAt = occurredAt;
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

  
}