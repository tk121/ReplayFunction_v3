package com.example.app.feature.replay.graphic.mapper;

import com.example.app.feature.replay.graphic.entity.AlertLog;
import com.example.app.feature.replay.graphic.model.ReplayAvduAlert;

public class AlertLogMapper {

    public ReplayAvduAlert toReplayAvduAlert(AlertLog row) {
        ReplayAvduAlert alert = new ReplayAvduAlert();

        alert.setAlertId(row.getAlertId());
        alert.setUnitNo(row.getUnitNo());
        alert.setAlertTag(row.getAlertTag());
        alert.setAlertName1(row.getAlertName1());
        alert.setAlertName2(row.getAlertName2());
        alert.setAlertSeverity(row.getAlertSeverity());
        alert.setColumnNo(row.getColumnNo());
        alert.setFirsthit(row.getFirsthit());
        alert.setFlick(row.getFlick());
        alert.setYokokuColor(row.getYokokuColor());

        return alert;
    }
}