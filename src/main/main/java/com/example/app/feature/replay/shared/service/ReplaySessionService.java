package com.example.app.feature.replay.shared.service;

import com.example.app.feature.replay.shared.model.ReplayState;

/**
 * 共有リプレイ状態保持の最小ひな形です。
 * 実プロジェクトでは既存の保持方式に合わせて差し替えてください。
 */
public class ReplaySessionService {

    private final ReplayState replayState = new ReplayState();

    public synchronized ReplayState getReplayState() {
        return replayState;
    }

    public synchronized void resetReplayState() {
        replayState.setPlaying(false);
        replayState.setCurrentReplayTime(null);
        replayState.setBaseStartTime(null);
        replayState.setTargetFrom(null);
        replayState.setTargetTo(null);
        replayState.setSpeed(1);
        replayState.setOperatorUserId(null);
        replayState.setLastTickTime(null);
        replayState.setLastTickDurationMs(0L);
    }

    public synchronized void clearCacheOnly() {
        /*
         * 実プロジェクトではここに以下のような初期化を追加してください。
         * - graphic 用メモリ状態クリア
         * - alert の有効一覧クリア
         * - plant data 現在値クリア
         */
    }
}
