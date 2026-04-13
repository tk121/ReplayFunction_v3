package com.example.app.feature.replay.common.engine;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.common.controller.ws.WsHub;
import com.example.app.feature.replay.common.model.ReplayMode;
import com.example.app.feature.replay.common.model.ReplayState;
import com.example.app.feature.replay.common.service.ReplayResponseService;
import com.example.app.feature.replay.common.service.ReplaySessionService;
import com.example.app.feature.replay.graphic.service.ReplayCoordinator;

/**
 * replay の時間進行を担当するエンジンです。
 *
 * <p>
 * 一定周期で tick を実行し、
 * 再生中の room について currentReplayTime を進め、
 * その時間範囲のイベントを ReplayCoordinator 経由で適用します。
 * </p>
 *
 * <p>
 * REALTIME では既存の replay 時刻進行と連動して、
 * event 集計済みテーブルの差分も共有状態へ取り込みます。
 * </p>
 */
public class ReplayEngine {

    private static final Logger log = LoggerFactory.getLogger(ReplayEngine.class);

    /** room ごとの state 管理サービス */
    private final ReplaySessionService sessionService;

    /** replay 制御サービス */
    private final ReplayCoordinator replayCoordinator;

    /** state からレスポンスを組み立てるサービス */
    private final ReplayResponseService responseService;

    /** WebSocket 配信ハブ */
    private final WsHub wsHub;

    /** 1秒周期で replay を進めるスケジューラ */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ReplayEngine(
            ReplaySessionService sessionService,
            ReplayCoordinator replayCoordinator,
            ReplayResponseService responseService,
            WsHub wsHub) {

        this.sessionService = sessionService;
        this.replayCoordinator = replayCoordinator;
        this.responseService = responseService;
        this.wsHub = wsHub;
    }

    /**
     * replay エンジンを起動します。
     */
    public void start() {
        log.info("リプレイ処理を開始します");

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    /**
     * replay エンジンを停止します。
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * 1回分の replay 進行処理を行います。
     */
    private void tick() {
        Collection<ReplayState> states = sessionService.getAllStates();

        for (ReplayState state : states) {
            try {
                synchronized (state) {
                    if (state.getReplayMode() == ReplayMode.REALTIME) {
                        state.setPlayStatus(ReplayState.STATUS_PLAYING);
                    }

                    if (!ReplayState.STATUS_PLAYING.equals(state.getPlayStatus())) {
                        continue;
                    }

                    LocalDateTime current = state.getCurrentReplayTime();
                    LocalDateTime next;

                    if (state.getReplayMode() == ReplayMode.REALTIME) {
                        next = LocalDateTime.now().withNano(0);

                        if (current == null) {
                            state.setCurrentReplayTime(next);
                            continue;
                        }

                        if (!next.isAfter(current)) {
                            continue;
                        }
                    } else {
                        if (current == null) {
                            current = state.getStartDateTime();
                            state.setCurrentReplayTime(current);
                        }
                        next = current.plusSeconds(state.getSpeed());
                    }

                    // 既存 replay 処理
                    replayCoordinator.applyReplayRange(state, current, next);

                    // 再生時刻を更新
                    state.setCurrentReplayTime(next);

                    // REALTIME かつ条件反映済みなら event 集計済み差分を取り込む
                    if (state.getReplayMode() == ReplayMode.REALTIME && state.isConditionApplied()) {
                        AppRuntime.getReplayEventService().loadRealtimeAppendAndMerge(state);
                    }
                }

                wsHub.broadcast(state, responseService);

            } catch (Exception e) {
                log.error("処理中にエラーが発生しました", e);

                try {
                    synchronized (state) {
                        state.setPlayStatus(ReplayState.STATUS_STOPPED);
                        state.setSpeed(1);
                        state.setLastCommand("AUTO_STOP_ON_ENGINE_ERROR");
                    }
                    wsHub.broadcast(state, responseService);
                } catch (Exception ignore) {
                    System.out.println("ReplayEngine tick stop error => " + ignore.getMessage());
                }
            }
        }
    }
}