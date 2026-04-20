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
 * shared replay time を進め、必要な replay 適用処理を行った後、
 * trend module へ時刻進行通知も行います。
 * </p>
 */
public class ReplayEngine {

	private static final Logger log = LoggerFactory.getLogger(ReplayEngine.class);

	private final ReplaySessionService sessionService;
	private final ReplayCoordinator replayCoordinator;
	private final ReplayResponseService responseService;
	private final WsHub wsHub;

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

	public void start() {
		log.info("リプレイ処理を開始します");

		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				tick();
			}
		}, 1L, 1L, TimeUnit.SECONDS);
	}

	public void shutdown() {
		scheduler.shutdownNow();
	}

	private void tick() {
		Collection<ReplayState> states = sessionService.getAllStates();

		for (ReplayState state : states) {
			try {
				boolean advanced = false;

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

					replayCoordinator.applyReplayRange(state, current, next);

					state.setCurrentReplayTime(next);

					if (state.getReplayMode() == ReplayMode.REALTIME && state.isConditionApplied()) {
						AppRuntime.getReplayModule()
								.getReplayEventService()
								.loadRealtimeAppendAndMerge(state);
					}

					advanced = true;
				}

				wsHub.broadcast(state, responseService);

				if (advanced && AppRuntime.getTrendModule() != null) {
					try {
						AppRuntime.getTrendModule()
								.getTrendRealtimePushService()
								.onReplayTimeAdvanced();
					} catch (Exception trendEx) {
						log.error("Trend realtime push failed", trendEx);
					}
				}

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