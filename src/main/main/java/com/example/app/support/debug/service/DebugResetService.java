package com.example.app.support.debug.service;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.shared.service.ReplaySessionService;
import com.example.app.feature.replay.ws.service.WsHub;
import com.example.app.support.debug.dto.DebugResetRequestDto;
import com.example.app.support.debug.dto.DebugResetResultDto;
import com.example.app.support.debug.model.DebugResetType;

/**
 * デバッグ用リセットサービスです。
 */
public class DebugResetService {

    public DebugResetResultDto reset(DebugResetRequestDto request) {
        DebugResetResultDto result = new DebugResetResultDto();

        try {
            ReplaySessionService replaySessionService = AppRuntime.getReplaySessionService();
            WsHub wsHub = AppRuntime.getWsHub();

            if (replaySessionService == null) {
                throw new IllegalStateException("ReplaySessionService is null.");
            }

            DebugResetType type = DebugResetType.valueOf(request.getResetType());

            switch (type) {
                case REPLAY_STATE:
                    replaySessionService.resetReplayState();
                    break;
                case CACHE_ONLY:
                    replaySessionService.clearCacheOnly();
                    break;
                case WS_SESSION_ONLY:
                    if (wsHub != null) {
                        wsHub.closeAllSessions();
                    }
                    break;
                case ALL:
                    replaySessionService.resetReplayState();
                    replaySessionService.clearCacheOnly();
                    if (wsHub != null) {
                        wsHub.closeAllSessions();
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported resetType: " + type);
            }

            result.setSuccess(true);
            result.setMessage("debug reset executed. resetType=" + type.name());

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return result;
    }
}
