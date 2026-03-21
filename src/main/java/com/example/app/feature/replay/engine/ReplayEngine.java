package com.example.app.feature.replay.engine;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.feature.replay.controller.ws.WsHub;
import com.example.app.feature.replay.model.ReplayState;
import com.example.app.feature.replay.service.ReplayCoordinator;
import com.example.app.feature.replay.service.ReplayResponseService;
import com.example.app.feature.replay.service.ReplaySessionService;

/**
 * replay の時間進行を担当するエンジンです。
 *
 * <p>
 * 一定周期で tick を実行し、
 * 再生中の room について currentReplayTime を進め、
 * その時間範囲の event_log を ReplayCoordinator 経由で適用します。
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
	 *
	 * <p>
	 * 1秒ごとに tick を実行します。
	 * </p>
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
	 *
	 * <p>
	 * 各 room の状態を見て、再生中なら時間を進め、
	 * その区間のイベントを適用します。
	 * </p>
	 */
	private void tick() {
		Collection<ReplayState> states = sessionService.getAllStates();

		for (ReplayState state : states) {
			try {
				synchronized (state) {
					// 再生中でなければ何もしない
					if (!ReplayState.STATUS_PLAYING.equals(state.getPlayStatus())) {
						continue;
					}

					// 現在時刻と末尾時刻を求める
					LocalDateTime current = sessionService.parseDateTime(state.getCurrentReplayTime());
					LocalDateTime tail = sessionService.calcTailDateTime(state);

					// speed 倍で次時刻を計算する
					LocalDateTime next = current.plusSeconds(state.getSpeed());

					// 末尾を超えないように補正
					if (next.isAfter(tail)) {
						next = tail;
					}

					// current ～ next の間に発生したイベントを順番に適用
					replayCoordinator.applyReplayWindow(state, current, next);

					// replay の現在位置を進める
					state.setCurrentReplayTime(sessionService.formatDateTime(next));

					// 末尾まで到達したら自動停止
					if (!next.isBefore(tail)) {
						state.setPlayStatus(ReplayState.STATUS_STOPPED);
						state.setSpeed(1);
						state.setLastCommand("AUTO_STOP_AT_TAIL");
					}
				}

				// 更新後の状態を WebSocket で配信
				wsHub.broadcast(state, responseService);

			} catch (Exception e) {

				System.out.println("ReplayEngine tick => " + e.getMessage());
				log.error("処理中にエラーが発生しました", e);


				try {
					// 例外時は安全側で停止させる
					synchronized (state) {
						state.setPlayStatus(ReplayState.STATUS_STOPPED);
						state.setSpeed(1);
						state.setLastCommand("AUTO_STOP_ON_ENGINE_ERROR");
					}

					wsHub.broadcast(state, responseService);

				} catch (Exception ignore) {

					// 必要ならログ出力
					System.out.println("ReplayEngine tick stop error => " + ignore.getMessage());
				}
			}
		}
	}
}