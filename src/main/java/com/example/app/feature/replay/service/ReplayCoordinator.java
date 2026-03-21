package com.example.app.feature.replay.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.feature.replay.c.CInvoker;
import com.example.app.feature.replay.c.CRequest;
import com.example.app.feature.replay.c.CResult;
import com.example.app.feature.replay.controller.ws.WsClient;
import com.example.app.feature.replay.controller.ws.WsHub;
import com.example.app.feature.replay.dto.ReplayControlRequest;
import com.example.app.feature.replay.dto.ReplayStateResponse;
import com.example.app.feature.replay.entity.AlertLog;
import com.example.app.feature.replay.entity.OperationLog;
import com.example.app.feature.replay.mapper.AlertLogMapper;
import com.example.app.feature.replay.mapper.OperationLogMapper;
import com.example.app.feature.replay.model.ReplayAvduAlert;
import com.example.app.feature.replay.model.ReplayOperationEvent;
import com.example.app.feature.replay.model.ReplayState;
import com.example.app.feature.replay.model.ReplayVduState;
import com.example.app.feature.replay.repository.AlertLogRepository;
import com.example.app.feature.replay.repository.OperationLogRepository;

/**
 * replay 機能の中核制御を担当するサービスです。
 *
 * <p>
 * このクラスは主に次の役割を持ちます。
 * </p>
 * <ul>
 *   <li>操作画面からのコマンドを受けて state を更新する</li>
 *   <li>operation_log のイベントを 1件ずつ replay として適用する</li>
 *   <li>C プロセスへイベントを渡して結果を state に反映する</li>
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

	/** operation_log → CRequest 変換用 Mapper */
	private final OperationLogMapper operationLogMapper;

	/** alert_log 取得用 Repository */
	private final AlertLogRepository alertLogRepository;

	/** alert_log → ReplayAvduAlert 変換用 Mapper */
	private final AlertLogMapper alertLogMapper;

	/** C プロセス呼び出しインターフェース */
	private final CInvoker cInvoker;

	public ReplayCoordinator(
			ReplaySessionService sessionService,
			ReplayResponseService responseService,
			WsHub wsHub,
			OperationLogRepository operationLogRepository,
			AlertLogRepository alertLogRepository,
			OperationLogMapper operationLogMapper,
			AlertLogMapper alertLogMapper,
			CInvoker cInvoker) {

		this.sessionService = sessionService;
		this.responseService = responseService;
		this.wsHub = wsHub;
		this.operationLogRepository = operationLogRepository;
		this.alertLogRepository = alertLogRepository;
		this.operationLogMapper = operationLogMapper;
		this.alertLogMapper = alertLogMapper;
		this.cInvoker = cInvoker;
	}

	/**
	 * 操作画面からのコマンドを処理します。
	 *
	 * <p>
	 * 再生条件の適用、再生開始、停止、早送り、先頭移動、末尾移動などを処理し、
	 * 更新後の状態を WebSocket で全表示画面へ配信します。
	 * </p>
	 *
	 * @param req 操作リクエスト
	 * @param remoteIp 操作者IP
	 * @return 更新後の replay 状態
	 * @throws Exception 処理失敗時
	 */
	public ReplayStateResponse handleControl(ReplayControlRequest req, String remoteIp) throws Exception {

		sessionService.validateControlRequest(req);
		String roomId = sessionService.normalizeRoomId(req.getRoomId());
		ReplayState state = sessionService.getOrCreate(roomId);

		// 共通項目（操作者、開始日時、期間など）を反映
		//sessionService.applyBaseFields(state, req, remoteIp);

		String command = req.getCommand();
		if (command == null || command.trim().length() == 0) {
			command = "APPLY_CONDITION";
		}

		// 操作権を確保する
		sessionService.ensureControlOwner(state, req.getClientId(), req.getOperatorName());

		// 現在操作者や表示条件を state に反映
		sessionService.applyBaseFields(state, req, remoteIp);

		synchronized (state) {
			if ("APPLY_CONDITION".equals(command)) {
				// 条件適用時は停止状態に戻し、開始位置を再設定する
				state.setPlayStatus(ReplayState.STATUS_STOPPED);
				state.setSpeed(1);
				state.setCurrentReplayTime(state.getStartDateTime());

				// その時点での最新 OPEN をもとに画面スナップショットだけ再構成する
				refreshAllSnapshots(state);

			} else if ("PLAY".equals(command)) {
				// 再生開始
				state.setPlayStatus(ReplayState.STATUS_PLAYING);

				// 現在位置が未設定なら開始日時を採用
				if (state.getCurrentReplayTime() == null || state.getCurrentReplayTime().length() == 0) {
					state.setCurrentReplayTime(state.getStartDateTime());
				}

				// speed が不正なら 1倍速に戻す
				if (state.getSpeed() <= 0) {
					state.setSpeed(1);
				}

			} else if ("STOP".equals(command)) {
				// 停止
				state.setPlayStatus(ReplayState.STATUS_STOPPED);

			} else if ("FAST_FORWARD".equals(command)) {
				// 早送り。速度だけ切り替える
				state.setPlayStatus(ReplayState.STATUS_PLAYING);
				state.setSpeed(nextSpeed(state.getSpeed()));

			} else if ("GO_HEAD".equals(command)) {
				// 先頭へ移動
				state.setPlayStatus(ReplayState.STATUS_STOPPED);
				state.setSpeed(1);
				state.setCurrentReplayTime(state.getStartDateTime());
				refreshAllSnapshots(state);

			} else if ("GO_TAIL".equals(command)) {
				// 末尾へ移動
				state.setPlayStatus(ReplayState.STATUS_STOPPED);
				state.setSpeed(1);
				LocalDateTime tail = sessionService.calcTailDateTime(state);
				state.setCurrentReplayTime(sessionService.formatDateTime(tail));
				refreshAllSnapshots(state);

			} else {
				throw new IllegalArgumentException("未対応コマンドです: " + command);
			}

			// 最後に実行したコマンド名を保存
			state.setLastCommand(command);
		}

		// 更新後の状態を全クライアントへ配信
		wsHub.broadcast(state, responseService);

		// HTTP 呼び出し元にも結果を返す
		return responseService.buildResponse(state, 0, req.getClientId());
	}

	/**
	 * heartbeat を処理します。
	 *
	 * <p>
	 * 現在の操作権保持者であれば heartbeat 時刻を更新し、
	 * 更新後状態を返します。
	 * </p>
	 */
	public ReplayStateResponse handleHeartbeat(String roomId, String clientId) throws Exception {
		if (clientId == null || clientId.trim().length() == 0) {
			throw new IllegalArgumentException("clientId は必須です");
		}

		ReplayState state = sessionService.getOrCreate(roomId);
		sessionService.heartbeat(state, clientId);

		return responseService.buildControlResponse(state, clientId);
	}

	public ReplayStateResponse getState(String roomId, String clientType, int vduNo, String clientId) throws Exception {
		ReplayState state = sessionService.getState(roomId);
		prepareCurrentDisplayState(state, clientType);

		if (WsClient.CLIENT_TYPE_AVDU.equals(clientType)) {
			return responseService.buildAvduResponse(state, clientId);
		}
		if (WsClient.CLIENT_TYPE_VDU.equals(clientType)) {
			return responseService.buildVduResponse(state, vduNo, clientId);
		}
		return responseService.buildControlResponse(state, clientId);
	}

	/**
	 * 現在状態を取得します。
	 */
	public ReplayStateResponse getState(String roomId, int vduNo, String clientId) throws Exception {
		ReplayState state = sessionService.getState(roomId);
		return responseService.buildVduResponse(state, vduNo, clientId);
	}

	/**
	 * 現在状態を取得します。
	 *
	 * @param roomId ルームID
	 * @param vduNo 対象VDU番号
	 * @return 現在状態
	 * @throws Exception 処理失敗時
	 */
	public ReplayStateResponse getState(String roomId, int vduNo) throws Exception {
		ReplayState state = sessionService.getState(roomId);
		return responseService.buildResponse(state, vduNo);
	}

	/**
	* WebSocket 初回接続時などに、その画面種別に必要な現在スナップショットを準備します。
	*/
	public void prepareCurrentDisplayState(ReplayState state, String clientType) throws Exception {
		synchronized (state) {
			if (WsClient.CLIENT_TYPE_AVDU.equals(clientType)) {
				refreshAvduSnapshotAt(state, sessionService.parseDateTime(state.getCurrentReplayTime()));
			} else if (WsClient.CLIENT_TYPE_VDU.equals(clientType)) {
				if (needsInitialVduSnapshot(state)) {
					refreshOpenSnapshotOnly(state);
				}
			}
		}
	}

	/**
	 * 指定時間範囲に含まれる operation_log を順番に適用します。
	 *
	 * <p>
	 * ReplayEngine の1tick分で、
	 * 前回時刻から今回時刻までのイベントを operation_id 順に処理するために使用します。
	 * </p>
	 *
	 * @param state 対象 state
	 * @param fromExclusive 前回時刻（この時刻は含まない）
	 * @param toInclusive 今回時刻（この時刻は含む）
	 * @throws Exception 処理失敗時
	 */
	public void applyReplayWindow(ReplayState state, LocalDateTime fromExclusive, LocalDateTime toInclusive)
			throws Exception {

		log.debug("ReplayCoordinator#applyEvents start");

		List<OperationLog> rows = operationLogRepository.findEventsBetween(fromExclusive, toInclusive);

		// 取得したイベントを順番に1件ずつ適用する
		for (OperationLog row : rows) {
			// VDU 以外の graphic_type はここでは適用しない
			if (row .getGraphicType() != null
					&& row .getGraphicType().trim().length() > 0
					&& !"VDU".equalsIgnoreCase(row .getGraphicType().trim())) {
				continue;
			}
			
			ReplayOperationEvent event = operationLogMapper.toReplayOperationEvent(row);
			applyReplayOperation(state, event);
		}

		// AVDU はイベント適用型ではなく、時点スナップショット型で再構築する
		refreshAvduSnapshotAt(state, toInclusive);

		log.debug("ReplayCoordinator#applyEvents end");
	}

	/**
	 * operation_log の1イベントを replay として適用します。
	 *
	 * <p>
	 * Java 側では event の適用結果だけを管理し、
	 * 実際の画面内部状態の更新は C プロセスに任せます。
	 * </p>
	 *
	 * @param state 対象 state
	 * @param event 適用対象イベント
	 * @throws Exception Cエラーなどの失敗時
	 */
	private void applyReplayOperation(ReplayState state, ReplayOperationEvent event) throws Exception {
		// C プロセスへ渡す JSON 用リクエストを組み立てる
		CRequest request = operationLogMapper.toCRequest(event);

		// C 側へイベントを渡して適用させる
		CResult result;
		//CResult result = cInvoker.execute(request);

		// 対象 VDU の状態を取得
		ReplayVduState vduState = state.getOrCreateVduState(event.getVduNo());

		// replay 全体と対象 VDU の last系情報を更新
		updateLastAppliedState(state, vduState, event);

		if (true) {
			// C 適用成功
			state.setLastApplyResult("SUCCESS");
			vduState.setLastApplyResult("SUCCESS");

			if ("OPEN".equals(event.getActionType())) {
				// OPEN の場合は Java 側でも pageId と表示URL を更新する
				vduState.setLastPageId(event.getPageId());

			} else if ((vduState.getLastPageId() == null || vduState.getLastPageId().length() == 0)
					&& event.getPageId() != null && event.getPageId().length() > 0) {
				// pageId がまだ空なら補完しておく
				vduState.setLastPageId(event.getPageId());
			}

		} else {
			// C 側失敗時は replay を安全側に倒して停止させる
			String message = result != null ? result.getMessage() : "C result is null";

			state.setLastApplyResult("FAIL");
			vduState.setLastApplyResult("FAIL");
			state.setPlayStatus(ReplayState.STATUS_STOPPED);
			state.setSpeed(1);
			state.setLastCommand("AUTO_STOP_ON_C_ERROR");

			throw new IllegalStateException("C処理失敗 eventId=" + event.getOperationId() + ", message=" + message);
		}
	}

	private void refreshAllSnapshots(ReplayState state) throws Exception {
		refreshOpenSnapshotOnly(state);
		refreshAvduSnapshotAt(state, sessionService.parseDateTime(state.getCurrentReplayTime()));
	}

	/**
	 * 指定再生時刻時点の最新 OPEN 情報だけをもとに、画面表示スナップショットを再構成します。
	 *
	 * <p>
	 * GO_HEAD / GO_TAIL / APPLY_CONDITION のときに、
	 * 少なくとも表示ページを復元するために使用します。
	 * </p>
	 *
	 * @param state 対象 state
	 * @throws Exception DB取得失敗時
	 */
	private void refreshOpenSnapshotOnly(ReplayState state) throws Exception {
		LocalDateTime replayTime = sessionService.parseDateTime(state.getCurrentReplayTime());
		Map<Integer, String> pageMap = operationLogRepository.findLatestOpenPageMap(replayTime);

		// 全体の最後の適用イベント情報をクリア
		state.setLastAppliedOperationId(null);
		state.setLastAppliedActionType(null);
		state.setLastAppliedVduNo(null);
		state.setLastApplyResult(null);
		state.setLastAppliedOccurredAt(null);
		state.setLastControlId(null);
		state.setLastButtonId(null);
		state.setLastValue(null);

		// 各 VDU の「最後に適用したイベント情報」だけクリア
		state.clearAllVduEventStatusOnly();

		for (int vduNo = 1; vduNo <= 7; vduNo++) {
			ReplayVduState vduState = state.getOrCreateVduState(vduNo);
			String pageId = pageMap.get(Integer.valueOf(vduNo));

			// その時点の最新 OPEN に基づいて pageId / URL を設定
			vduState.setLastPageId(pageId);
		}
	}

	private void refreshAvduSnapshotAt(ReplayState state, LocalDateTime replayTime) throws Exception {
		List<AlertLog> rows = alertLogRepository.findActiveAlertsAt(replayTime);

		List<ReplayAvduAlert> alerts = new ArrayList<ReplayAvduAlert>();
		for (AlertLog row : rows) {
			alerts.add(alertLogMapper.toReplayAvduAlert(row));
		}

		state.getAvduState().setAlerts(alerts);
	}

	private boolean needsInitialVduSnapshot(ReplayState state) {
		for (ReplayVduState vduState : state.getVduStateMap().values()) {
			if (vduState.getLastPageId() != null && vduState.getLastPageId().trim().length() > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * LocalDateTime を replay 用文字列へ変換します。
	 *
	 * @param occurredAt 発生時刻
	 * @return フォーマット済み文字列
	 */
	private String formatOccurredAt(LocalDateTime occurredAt) {
		if (occurredAt == null) {
			return null;
		}
		return sessionService.formatDateTime(occurredAt);
	}

	/**
	 * 早送り時の次の速度を返します。
	 *
	 * <p>
	 * 1 → 2 → 4 → 8 → 1 の順で切り替えます。
	 * </p>
	 *
	 * @param current 現在速度
	 * @return 次速度
	 */
	private int nextSpeed(int current) {
		if (current <= 1) {
			return 2;
		}
		if (current == 2) {
			return 4;
		}
		if (current == 4) {
			return 8;
		}
		return 1;
	}


	private void updateLastAppliedState(ReplayState state, ReplayVduState vduState, ReplayOperationEvent  operate) {

		// replay 全体として最後に適用したイベント情報
		state.setLastAppliedOperationId(operate.getOperationId());
		state.setLastAppliedActionType(operate.getActionType());
		state.setLastAppliedVduNo(operate.getVduNo());
		state.setLastAppliedOccurredAt(formatOccurredAt(operate.getOccurredAt()));
		state.setLastControlId(operate.getControlId());
		state.setLastButtonId(operate.getButtonId());
		state.setLastValue(operate.getValue());

		// 対象 VDU の最後の適用イベント情報
		vduState.setLastAppliedOperationId(Long.valueOf(operate.getOperationId()));
		vduState.setLastAppliedActionType(operate.getActionType());
		vduState.setLastAppliedVduNo(Integer.valueOf(operate.getVduNo()));
		vduState.setLastAppliedOccurredAt(formatOccurredAt(operate.getOccurredAt()));
		vduState.setLastControlId(operate.getControlId());
		vduState.setLastSymbolId(operate.getButtonId());
		vduState.setLastValue(operate.getValue());
	}
}