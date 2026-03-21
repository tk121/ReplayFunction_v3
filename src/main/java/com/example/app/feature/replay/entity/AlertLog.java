package com.example.app.feature.replay.entity;

import java.time.LocalDateTime;

/**
 * AVDU 用アラート履歴テーブルの1レコードです。
 */
public class AlertLog {

	private String alertId;
	private Integer unitNo;
	private LocalDateTime occurredAt;
	private String actionType;
	private String alertTag;
	private String alertName1;
	private String alertName2;
	private String alertSeverity;
	private Integer columnNo;
	private boolean firsthit;
	private boolean flick;
	private String yokokuColor;

	public String getAlertId() {
		return alertId;
	}

	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}

	public Integer getUnitNo() {
		return unitNo;
	}

	public void setUnitNo(Integer unitNo) {
		this.unitNo = unitNo;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(LocalDateTime occuredAt) {
		this.occurredAt = occuredAt;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getAlertTag() {
		return alertTag;
	}

	public void setAlertTag(String alertTag) {
		this.alertTag = alertTag;
	}

	public String getAlertName1() {
		return alertName1;
	}

	public void setAlertName1(String alertName1) {
		this.alertName1 = alertName1;
	}

	public String getAlertName2() {
		return alertName2;
	}

	public void setAlertName2(String alertName2) {
		this.alertName2 = alertName2;
	}

	public String getAlertSeverity() {
		return alertSeverity;
	}

	public void setAlertSeverity(String alertSeverity) {
		this.alertSeverity = alertSeverity;
	}

	public Integer getColumnNo() {
		return columnNo;
	}

	public void setColumnNo(Integer columnNo) {
		this.columnNo = columnNo;
	}

	public boolean getFirsthit() {
		return firsthit;
	}

	public void setFirsthit(boolean firsthit) {
		this.firsthit = firsthit;
	}

	public boolean getFlick() {
		return flick;
	}

	public void setFlick(boolean flick) {
		this.flick = flick;
	}

	public String getYokokuColor() {
		return yokokuColor;
	}

	public void setYokokuColor(String yokokuColor) {
		this.yokokuColor = yokokuColor;
	}
}