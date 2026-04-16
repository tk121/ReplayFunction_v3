package com.example.app.common.runtime;

import com.example.app.feature.replay.control.service.ReplayCommandService;
import com.example.app.feature.replay.shared.service.ReplaySessionService;
import com.example.app.feature.replay.ws.service.WsHub;

/**
 * アプリ起動時に生成した主要サービスの共有保持クラス。
 * 実プロジェクトでは既存の AppRuntime に合わせて調整してください。
 */
public final class AppRuntime {

    private static ReplaySessionService replaySessionService;
    private static ReplayCommandService replayCommandService;
    private static WsHub wsHub;

    private AppRuntime() {
    }

    public static ReplaySessionService getReplaySessionService() {
        return replaySessionService;
    }

    public static void setReplaySessionService(ReplaySessionService replaySessionService) {
        AppRuntime.replaySessionService = replaySessionService;
    }

    public static ReplayCommandService getReplayCommandService() {
        return replayCommandService;
    }

    public static void setReplayCommandService(ReplayCommandService replayCommandService) {
        AppRuntime.replayCommandService = replayCommandService;
    }

    public static WsHub getWsHub() {
        return wsHub;
    }

    public static void setWsHub(WsHub wsHub) {
        AppRuntime.wsHub = wsHub;
    }
}
