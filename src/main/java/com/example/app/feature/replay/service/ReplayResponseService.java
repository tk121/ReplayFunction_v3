package com.example.app.feature.replay.service;

import com.example.app.feature.replay.dto.ReplayStateResponse;
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
     * 旧呼び出し互換用です。
     */
    public ReplayStateResponse buildResponse(ReplayState state, int vduNo) {
        return buildResponse(state, vduNo, null);
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
    public ReplayStateResponse buildResponse(ReplayState state, int vduNo, String clientId) {
        synchronized (state) {
            ReplayStateResponse res = new ReplayStateResponse();

            // replay 全体に共通する状態
            res.setRoomId(state.getRoomId());
            res.setOperatorName(state.getOperatorName());
            res.setOperatorIp(state.getOperatorIp());
            res.setPlayStatus(state.getPlayStatus());
            res.setStartDateTime(state.getStartDateTime());
            res.setPeriodHours(state.getPeriodHours());
            res.setCurrentReplayTime(state.getCurrentReplayTime());
            res.setSpeed(state.getSpeed());
            res.setLastCommand(state.getLastCommand());
            
            // 排他制御表示用
            res.setControllerUserName(state.getControllerUserName());
            res.setCanOperate(sessionService.canOperate(state, clientId));

            if (vduNo > 0) {
                // 指定VDUの個別状態をレスポンスへ反映する
                ReplayVduState vduState = state.getOrCreateVduState(vduNo);

                res.setSelectedVduNo(vduNo);
                res.setDisplayUrl(vduState.getDisplayUrl());
                res.setCurrentPageId(vduState.getCurrentPageId());
                res.setLastAppliedEventId(vduState.getLastAppliedEventId());
                res.setLastAppliedEventType(vduState.getLastAppliedEventType());
                res.setLastAppliedVduNo(vduState.getLastAppliedVduNo());
                res.setLastApplyResult(vduState.getLastApplyResult());
                res.setLastAppliedOccurredAt(vduState.getLastAppliedOccurredAt());
                res.setLastControlId(vduState.getLastControlId());
                res.setLastSymbolId(vduState.getLastSymbolId());
                res.setLastValue(vduState.getLastValue());

            } else {
                // replay 全体の最後の適用結果を返す
                res.setSelectedVduNo(0);
                res.setLastAppliedEventId(state.getLastAppliedEventId());
                res.setLastAppliedEventType(state.getLastAppliedEventType());
                res.setLastAppliedVduNo(state.getLastAppliedVduNo());
                res.setLastApplyResult(state.getLastApplyResult());
                res.setLastAppliedOccurredAt(state.getLastAppliedOccurredAt());
                res.setLastControlId(state.getLastControlId());
                res.setLastSymbolId(state.getLastSymbolId());
                res.setLastValue(state.getLastValue());
            }

            return res;
        }
    }
}