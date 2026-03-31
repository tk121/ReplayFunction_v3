package com.example.app.feature.replay.graphic.c.plant;

import java.math.BigDecimal;

/**
 * Plant 非同期依頼の1件分です。
 */
public class PlantAsyncRequestItem {

    private String symbol;
    private BigDecimal aiValue;

    public PlantAsyncRequestItem() {
    }

    public PlantAsyncRequestItem(String symbol, BigDecimal aiValue) {
        this.symbol = symbol;
        this.aiValue = aiValue;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getAiValue() {
        return aiValue;
    }

    public void setAiValue(BigDecimal aiValue) {
        this.aiValue = aiValue;
    }
}