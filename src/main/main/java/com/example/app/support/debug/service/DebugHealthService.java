package com.example.app.support.debug.service;

import java.sql.Connection;

import javax.sql.DataSource;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.control.service.ReplayCommandService;
import com.example.app.feature.replay.shared.service.ReplaySessionService;
import com.example.app.feature.replay.ws.service.WsHub;
import com.example.app.support.debug.dto.DebugHealthDto;

/**
 * 簡易ヘルスチェックサービスです。
 */
public class DebugHealthService {

    private final DataSource dataSource;

    public DebugHealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DebugHealthDto getHealth() {
        DebugHealthDto dto = new DebugHealthDto();

        ReplaySessionService replaySessionService = AppRuntime.getReplaySessionService();
        ReplayCommandService replayCommandService = AppRuntime.getReplayCommandService();
        WsHub wsHub = AppRuntime.getWsHub();

        dto.setAppStatus("OK");
        dto.setDatabaseStatus(checkDatabase());
        dto.setReplayEngineStatus(replaySessionService != null ? "OK" : "NG");
        dto.setWsHubStatus(wsHub != null ? "OK" : "NG");

        if (replayCommandService == null) {
            dto.setMessage("ReplayCommandService is null.");
        } else {
            dto.setMessage("debug health check passed.");
        }

        return dto;
    }

    private String checkDatabase() {
        if (dataSource == null) {
            return "NG(DataSource is null)";
        }

        try (Connection con = dataSource.getConnection()) {
            return con.isValid(2) ? "OK" : "NG(invalid connection)";
        } catch (Exception e) {
            return "NG(" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
        }
    }
}
