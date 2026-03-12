package com.example.app.feature.replay.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.app.feature.replay.c.CInvoker;
import com.example.app.feature.replay.c.CRequest;
import com.example.app.feature.replay.c.CResult;
import com.example.app.feature.replay.controller.ws.WsHub;
import com.example.app.feature.replay.dto.ReplayControlRequest;
import com.example.app.feature.replay.dto.ReplayStateResponse;
import com.example.app.feature.replay.entity.EventLog;
import com.example.app.feature.replay.model.ReplayState;
import com.example.app.feature.replay.model.ReplayVduState;
import com.example.app.feature.replay.repository.EventLogRepository;

/**
 * replay 機能の中核制御を担当するサービスです。
 *
 * <p>
 * このクラスは主に次の役割を持ちます。
 * </p>
 * <ul>
 *   <li>操作画面からのコマンドを受けて state を更新する</li>
 *   <li>event_log のイベントを 1件ずつ replay として適用する</li>
 *   <li>C プロセスへイベントを渡して結果を state に反映する</li>
 *   <li>必要に応じて WebSocket で状態を配信する</li>
 * </ul>
 */
public class ReplayCoordinator {

    /** room ごとの state 管理サービス */
    private final ReplaySessionService sessionService;

    /** state からレスポンスを組み立てるサービス */
    private final ReplayResponseService responseService;

    /** WebSocket クライアントへの配信ハブ */
    private final WsHub wsHub;

    /** event_log 取得用 Repository */
    private final EventLogRepository eventLogRepository;

    /** C プロセス呼び出しインターフェース */
    private final CInvoker cInvoker;

    public ReplayCoordinator(
            ReplaySessionService sessionService,
            ReplayResponseService responseService,
            WsHub wsHub,
            EventLogRepository eventLogRepository,
            CInvoker cInvoker) {

        this.sessionService = sessionService;
        this.responseService = responseService;
        this.wsHub = wsHub;
        this.eventLogRepository = eventLogRepository;
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
        String roomId = sessionService.normalizeRoomId(req.getRoomId());
        ReplayState state = sessionService.getOrCreate(roomId);

        // 共通項目（操作者、開始日時、期間など）を反映
        sessionService.applyBaseFields(state, req, remoteIp);

        String command = req.getCommand();
        if (command == null || command.trim().length() == 0) {
            command = "APPLY_CONDITION";
        }

        synchronized (state) {
            if ("APPLY_CONDITION".equals(command)) {
                // 条件適用時は停止状態に戻し、開始位置を再設定する
                state.setPlayStatus(ReplayState.STATUS_STOPPED);
                state.setSpeed(1);
                state.setCurrentReplayTime(state.getStartDateTime());

                // その時点での最新 OPEN をもとに画面スナップショットだけ再構成する
                refreshOpenSnapshotOnly(state);

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
                refreshOpenSnapshotOnly(state);

            } else if ("GO_TAIL".equals(command)) {
                // 末尾へ移動
                state.setPlayStatus(ReplayState.STATUS_STOPPED);
                state.setSpeed(1);
                LocalDateTime tail = sessionService.calcTailDateTime(state);
                state.setCurrentReplayTime(sessionService.formatDateTime(tail));
                refreshOpenSnapshotOnly(state);

            } else {
                throw new IllegalArgumentException("未対応コマンドです: " + command);
            }

            // 最後に実行したコマンド名を保存
            state.setLastCommand(command);
        }

        // 更新後の状態を全クライアントへ配信
        wsHub.broadcast(state, responseService);

        // HTTP 呼び出し元にも結果を返す
        return responseService.buildResponse(state, 0);
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
     * 指定時間範囲に含まれる event_log を順番に適用します。
     *
     * <p>
     * ReplayEngine の1tick分で、
     * 前回時刻から今回時刻までのイベントを event_id 順に処理するために使用します。
     * </p>
     *
     * @param state 対象 state
     * @param fromExclusive 前回時刻（この時刻は含まない）
     * @param toInclusive 今回時刻（この時刻は含む）
     * @throws Exception 処理失敗時
     */
    public void applyReplayWindow(ReplayState state, LocalDateTime fromExclusive, LocalDateTime toInclusive) throws Exception {
        List<EventLog> events = eventLogRepository.findEventsBetween(fromExclusive, toInclusive);

        // 取得したイベントを順番に1件ずつ適用する
        for (EventLog event : events) {
            applyReplayEvent(state, event);
        }
    }

    /**
     * event_log の1イベントを replay として適用します。
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
    private void applyReplayEvent(ReplayState state, EventLog event) throws Exception {
        // C プロセスへ渡す JSON 用リクエストを組み立てる
        CRequest request = new CRequest();
        request.setEventType(event.getEventType());
        request.setPageId(event.getPageId());
        request.setControlId(event.getControlId());
        request.setSymbolId(event.getSymbolId());
        request.setValue(event.getValue());

        // C 側へイベントを渡して適用させる
        CResult result = cInvoker.execute(request);

        // 対象 VDU の状態を取得
        ReplayVduState vduState = state.getOrCreateVduState(event.getVduNo());

        // replay 全体として最後に適用したイベント情報を更新
        state.setLastAppliedEventId(Long.valueOf(event.getEventId()));
        state.setLastAppliedEventType(event.getEventType());
        state.setLastAppliedVduNo(Integer.valueOf(event.getVduNo()));
        state.setLastAppliedOccurredAt(formatOccurredAt(event.getOccurredAt()));
        state.setLastControlId(event.getControlId());
        state.setLastSymbolId(event.getSymbolId());
        state.setLastValue(event.getValue());

        // 対象 VDU の最後の適用イベント情報も更新
        vduState.setLastAppliedEventId(Long.valueOf(event.getEventId()));
        vduState.setLastAppliedEventType(event.getEventType());
        vduState.setLastAppliedVduNo(Integer.valueOf(event.getVduNo()));
        vduState.setLastAppliedOccurredAt(formatOccurredAt(event.getOccurredAt()));
        vduState.setLastControlId(event.getControlId());
        vduState.setLastSymbolId(event.getSymbolId());
        vduState.setLastValue(event.getValue());

        if (result != null && result.isSuccess()) {
            // C 適用成功
            state.setLastApplyResult("SUCCESS");
            vduState.setLastApplyResult("SUCCESS");

            if ("OPEN".equals(event.getEventType())) {
                // OPEN の場合は Java 側でも pageId と表示URL を更新する
                vduState.setCurrentPageId(event.getPageId());
                vduState.setDisplayUrl(resolveDisplayUrl(event.getPageId()));

            } else if ((vduState.getCurrentPageId() == null || vduState.getCurrentPageId().length() == 0)
                    && event.getPageId() != null && event.getPageId().length() > 0) {
                // pageId がまだ空なら補完しておく
                vduState.setCurrentPageId(event.getPageId());
                vduState.setDisplayUrl(resolveDisplayUrl(event.getPageId()));
            }

        } else {
            // C 側失敗時は replay を安全側に倒して停止させる
            String message = result != null ? result.getMessage() : "C result is null";

            state.setLastApplyResult("FAIL");
            vduState.setLastApplyResult("FAIL");
            state.setPlayStatus(ReplayState.STATUS_STOPPED);
            state.setSpeed(1);
            state.setLastCommand("AUTO_STOP_ON_C_ERROR");

            throw new IllegalStateException("C処理失敗 eventId=" + event.getEventId() + ", message=" + message);
        }
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
        Map<Integer, String> pageMap = eventLogRepository.findLatestOpenPageMap(replayTime);

        // 全体の最後の適用イベント情報をクリア
        state.setLastAppliedEventId(null);
        state.setLastAppliedEventType(null);
        state.setLastAppliedVduNo(null);
        state.setLastApplyResult(null);
        state.setLastAppliedOccurredAt(null);
        state.setLastControlId(null);
        state.setLastSymbolId(null);
        state.setLastValue(null);

        // 各 VDU の「最後に適用したイベント情報」だけクリア
        state.clearAllVduEventStatusOnly();

        for (int vduNo = 1; vduNo <= 7; vduNo++) {
            ReplayVduState vduState = state.getOrCreateVduState(vduNo);
            String pageId = pageMap.get(Integer.valueOf(vduNo));

            // その時点の最新 OPEN に基づいて pageId / URL を設定
            vduState.setCurrentPageId(pageId);
            vduState.setDisplayUrl(resolveDisplayUrl(pageId));
        }
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

    /**
     * pageId から iframe 表示用 URL を解決します。
     *
     * <p>
     * pageId がそのまま URL ならそのまま返し、
     * 画面IDだけなら pages/xxx.html の形式へ変換します。
     * </p>
     *
     * @param pageId ページID
     * @return 表示用 URL
     */
    private String resolveDisplayUrl(String pageId) {
        if (pageId == null || pageId.trim().length() == 0) {
            return null;
        }

        String value = pageId.trim();

        if (value.startsWith("/") || value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }

        if (value.endsWith(".html") || value.endsWith(".jsp")) {
            return value;
        }

        return "pages/" + value + ".html";
    }
}