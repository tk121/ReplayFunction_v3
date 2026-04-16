package com.example.app.support.debug.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.shared.model.ReplayState;
import com.example.app.feature.replay.shared.service.ReplaySessionService;
import com.example.app.feature.replay.ws.service.WsHub;
import com.example.app.support.debug.dto.DebugReplayStateDto;

/**
 * リプレイ状態参照用サービスです。
 */
public class DebugReplayStateService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DebugReplayStateDto getReplayState() {
        ReplaySessionService replaySessionService = AppRuntime.getReplaySessionService();
        WsHub wsHub = AppRuntime.getWsHub();

        DebugReplayStateDto dto = new DebugReplayStateDto();

        if (replaySessionService == null) {
            dto.setReplayStatus("UNKNOWN");
            dto.setCurrentReplayTime("");
            dto.setBaseStartTime("");
            dto.setSpeed("");
            dto.setTargetFrom("");
            dto.setTargetTo("");
            dto.setOperatorUserId("");
            dto.setConnectedSessionCount(0);
            dto.setLastTickTime("");
            dto.setLastTickDurationMs(0L);
            return dto;
        }

        ReplayState state = replaySessionService.getReplayState();

        dto.setReplayStatus(state.isPlaying() ? "PLAY" : "STOP");
        dto.setCurrentReplayTime(format(state.getCurrentReplayTime()));
        dto.setBaseStartTime(format(state.getBaseStartTime()));
        dto.setSpeed(state.getSpeed() + "x");
        dto.setTargetFrom(format(state.getTargetFrom()));
        dto.setTargetTo(format(state.getTargetTo()));
        dto.setOperatorUserId(nullToEmpty(state.getOperatorUserId()));
        dto.setConnectedSessionCount(wsHub != null ? wsHub.getSessionCount() : 0);
        dto.setLastTickTime(format(state.getLastTickTime()));
        dto.setLastTickDurationMs(state.getLastTickDurationMs());

        return dto;
    }

    private String format(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(FORMATTER);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
