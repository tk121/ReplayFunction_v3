package com.example.app.feature.replay.service;

import java.util.ArrayList;

import com.example.app.feature.replay.dto.ReplayStateResponse;
import com.example.app.feature.replay.model.ReplayAvduState;
import com.example.app.feature.replay.model.ReplayState;
import com.example.app.feature.replay.model.ReplayVduState;

/**
 * ReplayState から、画面返却用の ReplayStateResponse を組み立てるサービスです。
 *
 * <p>
 * このクラスの役割は「state を DTO に変換すること」です。
 * DBアクセスや C プロセス呼び出しはここでは行わず、
 * すでに確定している state の内容をレスポンスへ詰め替えます。
 * </p>
 */
public class ReplayResponseService {

	/** 状態管理サービス */
	private final ReplaySessionService sessionService;

	public ReplayResponseService(ReplaySessionService sessionService) {
		this.sessionService = sessionService;
	}

	/**
	 * 既存互換用。
	 */
	public ReplayStateResponse buildResponse(ReplayState state, int vduNo) {
		return buildVduResponse(state, vduNo, null);
	}

	/**
	 * 既存互換用。
	 */
	public ReplayStateResponse buildResponse(ReplayState state, int vduNo, String clientId) {
		return buildVduResponse(state, vduNo, clientId);
	}

	/**
	 * ReplayState からレスポンスDTOを生成します。
	 *
	 * <p>
	 * vduNo が 0 の場合は replay 全体の状態を返し、
	 * 1以上の場合は指定 VDU の表示状態を含めて返します。
	 * </p>
	 *
	 * @param state 現在の replay 状態
	 * @param vduNo 対象 VDU 番号。0 の場合は全体状態
	 * @param clientId このレスポンスを受け取るクライアントID
	 * @return 画面返却用レスポンス
	 */
    public ReplayStateResponse buildControlResponse(ReplayState state, String clientId) {
		synchronized (state) {
			ReplayStateResponse res = new ReplayStateResponse();
			populateCommon(res, state, clientId);
			res.setClientType("CONTROL");
			res.setSelectedVduNo(0);
			populateWholeState(res, state);
			return res;
		}
	}

	public ReplayStateResponse buildVduResponse(ReplayState state, int vduNo, String clientId) {
		synchronized (state) {
			ReplayStateResponse res = new ReplayStateResponse();
			populateCommon(res, state, clientId);
			res.setClientType("VDU");

			ReplayVduState vduState = state.getOrCreateVduState(vduNo);
			res.setSelectedVduNo(vduNo);
			res.setLastPageId(vduState.getLastPageId());
			res.setLastAppliedOperationId(vduState.getLastAppliedOperationId());
			res.setLastAppliedActionType(vduState.getLastAppliedActionType());
			res.setLastAppliedVduNo(vduState.getLastAppliedVduNo());
			res.setLastApplyResult(vduState.getLastApplyResult());
			res.setLastAppliedOccurredAt(vduState.getLastAppliedOccurredAt());
			res.setLastControlId(vduState.getLastControlId());
			res.setLastButtonId(vduState.getLastSymbolId());
			res.setLastValue(vduState.getLastValue());
			return res;
		}
	}

	public ReplayStateResponse buildAvduResponse(ReplayState state, String clientId) {
		synchronized (state) {
			ReplayStateResponse res = new ReplayStateResponse();
			populateCommon(res, state, clientId);
			res.setClientType("AVDU");
			res.setSelectedVduNo(0);

			ReplayAvduState avduState = state.getAvduState();
			if (avduState != null && avduState.getAlerts() != null) {
				res.setAvduAlerts(
						new ArrayList<com.example.app.feature.replay.model.ReplayAvduAlert>(avduState.getAlerts()));
			}
			return res;
		}
	}

	private void populateCommon(ReplayStateResponse res, ReplayState state, String clientId) {
		res.setRoomId(state.getRoomId());
		res.setOperatorName(state.getOperatorName());
		res.setOperatorIp(state.getOperatorIp());
		res.setPlayStatus(state.getPlayStatus());
		res.setStartDateTime(state.getStartDateTime());
		res.setPeriodHours(state.getPeriodHours());
		res.setCurrentReplayTime(state.getCurrentReplayTime());
		res.setSpeed(state.getSpeed());
		res.setLastCommand(state.getLastCommand());

		res.setControllerUserName(state.getControllerUserName());
		res.setCanOperate(sessionService.canOperate(state, clientId));
	}

	private void populateWholeState(ReplayStateResponse res, ReplayState state) {
		res.setLastAppliedOperationId(state.getLastAppliedOperationId());
		res.setLastAppliedActionType(state.getLastAppliedActionType());
		res.setLastAppliedVduNo(state.getLastAppliedVduNo());
		res.setLastApplyResult(state.getLastApplyResult());
		res.setLastAppliedOccurredAt(state.getLastAppliedOccurredAt());
		res.setLastControlId(state.getLastControlId());
		res.setLastButtonId(state.getLastButtonId());
		res.setLastValue(state.getLastValue());
	}
}