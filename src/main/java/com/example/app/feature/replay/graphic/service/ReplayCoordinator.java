package com.example.app.feature.replay.graphic.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.constant.ReplayControlCommand;
import com.example.app.feature.replay.common.controller.ws.WsClient;
import com.example.app.feature.replay.common.controller.ws.WsHub;
import com.example.app.feature.replay.common.model.ReplayMode;
import com.example.app.feature.replay.common.model.ReplayState;
import com.example.app.feature.replay.common.service.ReplayResponseService;
import com.example.app.feature.replay.common.service.ReplaySessionService;
import com.example.app.feature.replay.event.service.ReplayEventService;
import com.example.app.feature.replay.graphic.dto.ReplayControlRequest;
import com.example.app.feature.replay.graphic.dto.ReplayStateResponse;
import com.example.app.feature.replay.graphic.dto.external.GraphicProcessResponse;
import com.example.app.feature.replay.graphic.dto.external.TrendProcessResponse;
import com.example.app.feature.replay.graphic.entity.AlertLog;
import com.example.app.feature.replay.graphic.entity.OperationLog;
import com.example.app.feature.replay.graphic.entity.PlantDataLog;
import com.example.app.feature.replay.graphic.mapper.AlertLogMapper;
import com.example.app.feature.replay.graphic.mapper.OperationLogMapper;
import com.example.app.feature.replay.graphic.model.ReplayAvduAlert;
import com.example.app.feature.replay.graphic.model.ReplayOperationEvent;
import com.example.app.feature.replay.graphic.model.ReplayVduState;
import com.example.app.feature.replay.graphic.repository.AlertLogRepository;
import com.example.app.feature.replay.graphic.repository.OperationLogRepository;
import com.example.app.feature.replay.graphic.repository.PlantDataLogRepository;

/**
 * replay 機能の中核制御を担当するサービスです。
 *
 * <p>
 * このクラスは主に次の役割を持ちます。
 * </p>
 * <ul>
 *   <li>操作画面からのコマンドを受けて state を更新する</li>
 *   <li>operation_log のイベントを 1件ずつ replay として適用する</li>
 *   <li>必要に応じて event 集計済みデータを共有状態へロードする</li>
 *   <li>必要に応じて WebSocket で状態を配信する</li>
 * </ul>
 */
public class ReplayCoordinator {

	private static final Logger log = LoggerFactory.getLogger(ReplayCoordinator.class);

	/** room ごとの state 管理サービス */
	private final ReplaySessionService sessionService;

	/** state からレスポンスを組み立てるサービス */
	private final ReplayResponseService responseService;

	/** WebSocket クライアントへの配信ハブ */
	private final WsHub wsHub;

	/** operation_log 取得用 Repository */
	private final OperationLogRepository operationLogRepository;

	/** alert_log 取得用 Repository */
	private final AlertLogRepository alertLogRepository;

	/** plant_data_log 取得用 Repository */
	private final PlantDataLogRepository plantDataLogRepository;

	/** operation_log → replay event 変換用 Mapper */
	private final OperationLogMapper operationLogMapper;

	/** alert_log → ReplayAvduAlert 変換用 Mapper */
	private final AlertLogMapper alertLogMapper;

	/** 外部プロセス連携の窓口 */
	private final ReplayExternalProcessService externalProcessService;

	/** event 集計済みデータ取得サービス */
	private final ReplayEventService replayEventService;

	public ReplayCoordinator(
			ReplaySessionService sessionService,
			ReplayResponseService responseService,
			WsHub wsHub,
			OperationLogRepository operationLogRepository,
			AlertLogRepository alertLogRepository,
			PlantDataLogRepository plantDataLogRepository,
			OperationLogMapper operationLogMapper,
			AlertLogMapper alertLogMapper,
			ReplayExternalProcessService externalProcessService,
			ReplayEventService replayEventService) {

		this.sessionService = sessionService;
		this.responseService = responseService;
		this.wsHub = wsHub;
		this.operationLogRepository = operationLogRepository;
		this.alertLogRepository = alertLogRepository;
		this.plantDataLogRepository = plantDataLogRepository;
		this.operationLogMapper = operationLogMapper;
		this.alertLogMapper = alertLogMapper;
		this.externalProcessService = externalProcessService;
		this.replayEventService = replayEventService;
	}

	/**
	 * 操作画面からのコマンドを処理します。
	 *
	 * @param req 操作リクエスト
	 * @param remoteIp 操作者IP
	 * @param loginUser ログインユーザー
	 * @return 更新後の replay 状態
	 * @throws Exception 処理失敗時
	 */
	public ReplayStateResponse handleControl(ReplayControlRequest req, String remoteIp, LoginUser loginUser)
			throws Exception {
		sessionService.validateControlRequest(req);

		String roomId = sessionService.normalizeRoomId(req.getRoomId());
		ReplayState state = sessionService.getOrCreate(roomId);

		sessionService.assertCanOperate(state, loginUser);
		sessionService.applySharedFields(state, req, remoteIp);

		ReplayControlCommand command = normalizeCommand(req.getCommand());

		synchronized (state) {
			if (state.getReplayMode() == ReplayMode.REALTIME && command != ReplayControlCommand.APPLY_CONDITION) {
				throw new IllegalStateException("リアルタイム再生では再生制御ボタンは使用できません");
			}

			switch (command) {
			case APPLY_CONDITION:
				state.setPlayStatus(ReplayState.STATUS_STOPPED);
				state.setSpeed(1);

				if (state.getCurrentReplayTime() == null) {
					state.setCurrentReplayTime(state.getStartDateTime());
				}

				// 条件反映時に event 集計済みデータを共有状態へロード
				rebuildStateAt(state);
				replayEventService.loadAndStoreForState(state);

				break;

			case PLAY:
				state.setPlayStatus(ReplayState.STATUS_PLAYING);

				if (state.getCurrentReplayTime() == null) {
					state.setCurrentReplayTime(state.getStartDateTime());
				}

				if (state.getSpeed() <= 0) {
					state.setSpeed(1);
				}
				break;

			case STOP:
				state.setPlayStatus(ReplayState.STATUS_STOPPED);
				break;

			case CHANGE_SPEED:
				int requestedSpeed = normalizeSpeed(req.getSpeed());
				state.setSpeed(requestedSpeed);
				break;

			case GO_HEAD:
				state.setPlayStatus(ReplayState.STATUS_STOPPED);
				state.setSpeed(1);
				state.setCurrentReplayTime(state.getStartDateTime());

				rebuildStateAt(state);
				break;

			case GO_TAIL:
				state.setPlayStatus(ReplayState.STATUS_STOPPED);
				state.setSpeed(1);

				LocalDateTime tail = sessionService.calcTailDateTime(state, req.getDisplayHours().intValue());
				state.setCurrentReplayTime(tail);
				rebuildStateAt(state);
				break;

			default:
				throw new IllegalArgumentException("未対応コマンドです: " + command);
			}

			state.setLastCommand(command.name());
		}

		wsHub.broadcast(state, responseService);
		return responseService.buildControlResponse(state, loginUser);
	}

	public ReplayStateResponse getState(String roomId, String clientType, int vduNo, LoginUser loginUser)
			throws Exception {
		ReplayState state = sessionService.getState(roomId);
		prepareDisplayStateForClient(state, clientType);

		if (WsClient.CLIENT_TYPE_AVDU.equals(clientType)) {
			return responseService.buildAvduResponse(state, loginUser);
		}
		if (WsClient.CLIENT_TYPE_VDU.equals(clientType)) {
			return responseService.buildVduResponse(state, vduNo, loginUser);
		}
		return responseService.buildControlResponse(state, loginUser);
	}

	public void prepareDisplayStateForClient(ReplayState state, String clientType) throws Exception {
		synchronized (state) {
			LocalDateTime replayTime = state.getCurrentReplayTime();
			if (replayTime == null) {
				replayTime = state.getStartDateTime();
				state.setCurrentReplayTime(replayTime);
			}

			if (WsClient.CLIENT_TYPE_AVDU.equals(clientType)) {

				rebuildAvduStateAt(state, state.getCurrentReplayTime());

			} else if (WsClient.CLIENT_TYPE_VDU.equals(clientType)) {

				if (needsInitialVduState(state)) {

					rebuildVduStateAt(state);
					sendLatestPlantDataAt(state, replayTime);

				}
			}
		}
	}

	private void rebuildStateAt(ReplayState state) throws Exception {
		rebuildVduStateAt(state);
		sendLatestPlantDataAt(state, state.getCurrentReplayTime());
		rebuildAvduStateAt(state, state.getCurrentReplayTime());
	}

	/**
	 * 指定時間範囲に含まれる operation_log を順番に適用します。
	 *
	 * @param state 対象 state
	 * @param fromExclusive 前回時刻（この時刻は含まない）
	 * @param toInclusive 今回時刻（この時刻は含む）
	 * @throws Exception 処理失敗時
	 */
	public void applyReplayRange(ReplayState state, LocalDateTime fromExclusive, LocalDateTime toInclusive)
			throws Exception {

		applyOperationLogsInRange(state, fromExclusive, toInclusive);
		sendLatestPlantDataAt(state, toInclusive);
		rebuildAvduStateAt(state, toInclusive);
	}

	private void applyOperationLogsInRange(ReplayState state, LocalDateTime fromExclusive, LocalDateTime toInclusive)
			throws Exception {
		List<OperationLog> rows = loadOperationLogsInRange(state, fromExclusive, toInclusive);

		for (OperationLog row : rows) {
			if (row.getUnitNo() != null && state.getUnitNo() != null && !state.getUnitNo().equals(row.getUnitNo())) {
				continue;
			}
			if (row.getGraphicType() != null
					&& row.getGraphicType().trim().length() > 0
					&& !"VDU".equalsIgnoreCase(row.getGraphicType().trim())) {
				continue;
			}

			ReplayOperationEvent event = operationLogMapper.toReplayOperationEvent(row);
			applyOperationToVdu(state, event);
		}
	}

	private void applyOperationToVdu(ReplayState state, ReplayOperationEvent event) throws Exception {
		ReplayVduState vduState = state.getOrCreateVduState(event.getVduNo());

		updateLastAppliedState(state, vduState, event);

		state.setLastApplyResult("SUCCESS");
		vduState.setLastApplyResult("SUCCESS");

		if ("OPEN".equals(event.getActionType())) {
			vduState.setLastPageId(event.getPageId());
		} else if ((vduState.getLastPageId() == null || vduState.getLastPageId().length() == 0)
				&& event.getPageId() != null && event.getPageId().length() > 0) {
			vduState.setLastPageId(event.getPageId());
		}
	}

	/**
	 * 指定再生時刻時点の最新 OPEN 情報だけをもとに、画面表示スナップショットを再構成します。
	 */
	private void rebuildVduStateAt(ReplayState state) throws Exception {
		LocalDateTime replayTime = state.getCurrentReplayTime();
		Map<Integer, String> pageMap = operationLogRepository.findLatestOpenPageMap(replayTime);

		state.setLastAppliedOperationId(null);
		state.setLastAppliedActionType(null);
		state.setLastAppliedVduNo(null);
		state.setLastApplyResult(null);
		state.setLastAppliedOccurredAt(null);
		state.setLastControlId(null);
		state.setLastButtonId(null);
		state.setLastValue(null);

		state.clearAllVduEventStatusOnly();

		for (int vduNo = 1; vduNo <= 7; vduNo++) {
			ReplayVduState vduState = state.getOrCreateVduState(vduNo);
			vduState.setLastPageId(pageMap.get(Integer.valueOf(vduNo)));
		}
	}

	private void sendLatestPlantDataAt(ReplayState state, LocalDateTime replayTime) throws Exception {
		if (state.getUnitNo() == null) {
			log.warn("Skip plant_data_log because state.unitNo is null. replayTime={}",
					replayTime);
			return;
		}

		List<PlantDataLog> plantRows = plantDataLogRepository.findByUnitNoAndOccurredAtRange(state.getUnitNo(),
				replayTime);

		try {
			//			externalProcessService.submitPlantData(plantRows);
		} catch (Exception e) {
			throw new RuntimeException("Failed to submit plant data to external process", e);
		}
	}

	private void sendLatestPlantDataInRange(ReplayState state, LocalDateTime fromExclusive, LocalDateTime toInclusive)
			throws Exception {
		if (state.getUnitNo() == null) {

			return;
		}

		GraphicProcessResponse graphicResponse = externalProcessService.executeGraphic("REPLAY",
				toInclusive.toString());

		if (!graphicResponse.isSuccess()) {
			throw new IllegalStateException(
					"Graphic process failed. message=" + graphicResponse.getMessage());
		}
		TrendProcessResponse trendResponse = externalProcessService.executeTrend("REPLAY", toInclusive.toString());

		if (!trendResponse.isSuccess()) {
			throw new IllegalStateException(
					"Trend process failed. message=" + trendResponse.getMessage());
		}

		//        List<PlantDataLog> plantRows =
		//                plantDataLogRepository.findByUnitNoAndOccurredAtRange(state.getUnitNo(), replayTime);

		//		try {
		//			externalProcessService.submitPlantData(plantRows);
		//		} catch (Exception e) {
		//			throw new RuntimeException("Failed to submit plant data to external process", e);
		//		}
	}

	private void rebuildAvduStateAt(ReplayState state, LocalDateTime replayTime) throws Exception {
		List<AlertLog> rows = alertLogRepository.findActiveAlertsAt(replayTime);
		List<ReplayAvduAlert> alerts = new ArrayList<ReplayAvduAlert>();

		for (AlertLog row : rows) {
			if (row.getUnitNo() != null && state.getUnitNo() != null && !state.getUnitNo().equals(row.getUnitNo())) {
				continue;
			}
			alerts.add(alertLogMapper.toReplayAvduAlert(row));
		}

		state.getAvduState().setAlerts(alerts);
	}

	private List<OperationLog> loadOperationLogsInRange(ReplayState state, LocalDateTime fromExclusive,
			LocalDateTime toInclusive) throws Exception {
		List<OperationLog> rows = operationLogRepository.findEventsBetween(fromExclusive, toInclusive);
		List<OperationLog> filtered = new ArrayList<OperationLog>();

		for (OperationLog row : rows) {
			if (row.getUnitNo() != null && state.getUnitNo() != null && !state.getUnitNo().equals(row.getUnitNo())) {
				continue;
			}
			if (row.getGraphicType() != null
					&& row.getGraphicType().trim().length() > 0
					&& !"VDU".equalsIgnoreCase(row.getGraphicType().trim())) {
				continue;
			}
			filtered.add(row);
		}
		return filtered;
	}

	private List<AlertLog> loadActiveAlertsAt(ReplayState state, LocalDateTime replayTime) throws Exception {
		List<AlertLog> rows = alertLogRepository.findActiveAlertsAt(replayTime);
		List<AlertLog> filtered = new ArrayList<AlertLog>();

		for (AlertLog row : rows) {
			if (row.getUnitNo() != null && state.getUnitNo() != null && !state.getUnitNo().equals(row.getUnitNo())) {
				continue;
			}
			filtered.add(row);
		}
		return filtered;
	}

	private boolean needsInitialVduState(ReplayState state) {
		for (ReplayVduState vduState : state.getVduStateMap().values()) {
			if (vduState.getLastPageId() != null && vduState.getLastPageId().trim().length() > 0) {
				return false;
			}
		}
		return true;
	}

	//	private String normalizeCommand(String command) {
	//		if (command == null || command.trim().length() == 0) {
	//			return "APPLY_CONDITION";
	//		}
	//		return command.trim();
	//	}

	private ReplayControlCommand normalizeCommand(String command) {
		try {
			return ReplayControlCommand.from(command);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("未対応コマンドです: " + command, e);
		}
	}

	private int normalizeSpeed(Integer speed) {
		if (speed == null) {
			throw new IllegalArgumentException("速度が指定されていません。");
		}
		int value = speed.intValue();
		if (value != 1 && value != 2 && value != 4 && value != 8) {
			throw new IllegalArgumentException("速度は 1, 2, 4, 8 のいずれかで指定してください。");
		}
		return value;
	}

	private void updateLastAppliedState(ReplayState state, ReplayVduState vduState, ReplayOperationEvent operate) {
		state.setLastAppliedOperationId(Long.valueOf(operate.getOperationId()));
		state.setLastAppliedActionType(operate.getActionType());
		state.setLastAppliedVduNo(Integer.valueOf(operate.getVduNo()));
		state.setLastAppliedOccurredAt(operate.getOccurredAt());
		state.setLastControlId(operate.getControlId());
		state.setLastButtonId(operate.getButtonId());
		state.setLastValue(operate.getValue());

		vduState.setLastAppliedOperationId(Long.valueOf(operate.getOperationId()));
		vduState.setLastAppliedActionType(operate.getActionType());
		vduState.setLastAppliedVduNo(Integer.valueOf(operate.getVduNo()));
		vduState.setLastAppliedOccurredAt(operate.getOccurredAt());
		vduState.setLastControlId(operate.getControlId());
		vduState.setLastSymbolId(operate.getButtonId());
		vduState.setLastValue(operate.getValue());
	}
}