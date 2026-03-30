package com.example.app.feature.replay.graphic.model;

import java.util.ArrayList;
import java.util.List;

/**
 * AVDU の現在表示状態です。
 */
public class ReplayAvduState {

    private List<ReplayAvduAlert> alerts = new ArrayList<ReplayAvduAlert>();

    public List<ReplayAvduAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<ReplayAvduAlert> alerts) {
        this.alerts = alerts;
    }
}