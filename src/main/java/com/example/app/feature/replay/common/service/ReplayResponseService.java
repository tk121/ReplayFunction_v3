package com.example.app.feature.replay.common.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.model.ReplayState;
import com.example.app.feature.replay.graphic.dto.ReplayStateResponse;
import com.example.app.feature.replay.graphic.model.ReplayAvduState;
import com.example.app.feature.replay.graphic.model.ReplayVduState;

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
     * 操作画面向けレスポンスを生成します。
     */
    public ReplayStateResponse buildControlResponse(ReplayState state, LoginUser loginUser) {
        synchronized (state) {
            ReplayStateResponse res = new ReplayStateResponse();
            populateCommon(res, state, loginUser);
            res.setClientType("EVENT");
            res.setSelectedVduNo(0);
            populateWholeState(res, state);
            return res;
        }
    }

    /**
     * VDU 画面向けレスポンスを生成します。
     */
    public ReplayStateResponse buildVduResponse(ReplayState state, int vduNo, LoginUser loginUser) {
        synchronized (state) {
            ReplayStateResponse res = new ReplayStateResponse();
            populateCommon(res, state, loginUser);
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

    /**
     * AVDU 画面向けレスポンスを生成します。
     */
    public ReplayStateResponse buildAvduResponse(ReplayState state, LoginUser loginUser) {
        synchronized (state) {
            ReplayStateResponse res = new ReplayStateResponse();
            populateCommon(res, state, loginUser);
            res.setClientType("AVDU");
            res.setSelectedVduNo(0);

            ReplayAvduState avduState = state.getAvduState();
            if (avduState != null && avduState.getAlerts() != null) {
                res.setAvduAlerts(
                        new ArrayList<com.example.app.feature.replay.graphic.model.ReplayAvduAlert>(
                                avduState.getAlerts()));
            }
            return res;
        }
    }

    private void populateCommon(ReplayStateResponse res, ReplayState state, LoginUser loginUser) {
        res.setRoomId(state.getRoomId());
        res.setUnitNo(state.getUnitNo());
        res.setReplayMode(state.getReplayMode().name());
        res.setOperatorName(state.getOperatorName());
        res.setOperatorIp(state.getOperatorIp());
        res.setPlayStatus(state.getPlayStatus());
        res.setStartDateTime(state.getStartDateTime());
        res.setCurrentReplayTime(state.getCurrentReplayTime());
        res.setSpeed(state.getSpeed());
        res.setLastCommand(state.getLastCommand());
        res.setControllerUserName(state.getControllerUserName());

        res.setCanOperate(sessionService.canOperate(state, loginUser));
        res.setLoggedIn(loginUser != null);
        res.setCurrentUserName(loginUser != null ? loginUser.getUserName() : null);
        res.setCurrentUserCanControl(loginUser != null && loginUser.isCanControl());

        res.setConditionApplied(state.isConditionApplied());
        res.setEventSeries(copyEventSeries(state.getEventSeries()));
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

    private Map<String, Map<String, Integer>> copyEventSeries(
            Map<String, Map<String, Integer>> source) {

        Map<String, Map<String, Integer>> copied =
                new LinkedHashMap<String, Map<String, Integer>>();

        if (source == null) {
            return copied;
        }

        for (Map.Entry<String, Map<String, Integer>> entry : source.entrySet()) {
            copied.put(
                    entry.getKey(),
                    entry.getValue() == null
                            ? new LinkedHashMap<String, Integer>()
                            : new LinkedHashMap<String, Integer>(entry.getValue()));
        }

        return copied;
    }
}