package com.example.app.feature.replay.graphic.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlantDataLog {

    private Long dataId;
    private Integer unitNo;
    private LocalDateTime occurredAt;
    private String symbol;
    private String valueLocator;
    private String valueType;
    private BigDecimal aiValue;
    private Integer diValue;
    private String status;

    public Long getDataId() {
        return dataId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
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

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getValueLocator() {
        return valueLocator;
    }

    public void setValueLocator(String valueLocator) {
        this.valueLocator = valueLocator;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public BigDecimal getAiValue() {
        return aiValue;
    }

    public void setAiValue(BigDecimal aiValue) {
        this.aiValue = aiValue;
    }

    public Integer getDiValue() {
        return diValue;
    }

    public void setDiValue(Integer diValue) {
        this.diValue = diValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
