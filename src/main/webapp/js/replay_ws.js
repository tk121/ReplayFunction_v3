/**
 * WebSocket接続と状態管理モジュール
 * リプレイ機能のWebSocket通信を管理し、UIの状態更新を行う。
 */

// WebSocket接続インスタンス
let ws = null;
// 再接続タイマー
let reconnectTimer = null;
//                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  現在のルームID
let currentRoomId = null;
// 現在のクライアントタイプ（EVENT, VDU, AVDUなど）
let currentClientType = null;
// 現在のVDU番号
let currentVduNo = null;
// 現在のクライアントID
let currentClientId = null;

/**
 * ヘルパー関数
 */

/**
 * 指定されたIDのDOM要素を取得する
 * @param {string} id - 要素のID
 * @returns {HTMLElement|null} 取得した要素、またはnull
 */
const getById = (id) => document.getElementById(id);

/**
 * 指定されたIDの要素にテキストを設定する
 * null, undefined, 空文字列の場合は"-"を表示
 * @param {string} id - 要素のID
 * @param {string} value - 設定する値
 */
const setText = (id, value) => {
    const el = getById(id);
    if (el) el.textContent = (value === null || value === undefined || value === "") ? "-" : value;
};

/**
 * 指定されたIDの要素のdisabled属性を設定する
 * @param {string} id - 要素のID
 * @param {boolean} disabled - disabledにするかどうか
 */
const setDisabled = (id, disabled) => {
    const el = getById(id);
    if (el) el.disabled = !!disabled;
};

/**
 * ページの basePath を取得するユーティリティ
 */
const getBasePath = () => {
    const idx = location.pathname.lastIndexOf("/");
    return idx >= 0 ? location.pathname.substring(0, idx) : "";
};

/**
 * 操作可否に応じて、操作ボタンをグレーアウトします。
 * @param {Object} state - 現在の状態オブジェクト
 */
const applyOperateState = (state) => {
    const isRealtime = state && state.replayMode === "REALTIME";
    const disabled = !state || !state.canOperate;
    setDisabled("btnApply", disabled);
    setDisabled("btnPlay", disabled || isRealtime);
    setDisabled("btnStop", disabled || isRealtime);
    setDisabled("playSpeed", disabled || isRealtime);
    setDisabled("btnHead", disabled || isRealtime);
    setDisabled("btnTail", disabled || isRealtime);

    setText("currentControllerName", state ? state.controllerUserName : null);
    setText("currentUserName", state ? state.currentUserName : null);
    setText("currentUserRole", !state ? null : (state.loggedIn ? (state.currentUserCanControl ? "操作権保有可能" : "閲覧専用") : "未ログイン"));
    const msgEl = getById("canOperateMessage");
    if (msgEl) {
        if (!state) msgEl.textContent = "-";
        else if (state.canOperate) msgEl.textContent = "あなたは操作可能です。";
        else if (!state.loggedIn) msgEl.textContent = "未ログインのため操作できません。";
        else if (!state.currentUserCanControl) msgEl.textContent = "このユーザーは操作権を持てません。";
        else if (state.controllerUserName) msgEl.textContent = `現在は ${state.controllerUserName} が操作中です。`;
        else msgEl.textContent = "現在は操作できません。";
    }
};

/**
 * WebSocket再接続をスケジュールする
 * 既にタイマーが設定されている場合は何もしない
 */
const scheduleReconnect = () => {
    if (reconnectTimer) return;
    reconnectTimer = setTimeout(() => {
        reconnectTimer = null;
        connect(currentRoomId, currentClientType, currentVduNo, currentClientId);
    }, 3000);
};

/**
 * 再接続タイマーをクリアする
 */
const clearReconnectTimer = () => {
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }
};

/**
 * WebSocket接続を確立する
 * @param {string} roomId - ルームID
 * @param {string} clientType - クライアントタイプ
 * @param {string} vduNo - VDU番号
 * @param {string} clientId - クライアントID
 */
const connect = (roomId, clientType, vduNo, clientId) => {
    currentRoomId = roomId || "replayMode";
    currentClientType = clientType || "";
    currentVduNo = vduNo || "";
    currentClientId = clientId || "";

    // 既存の接続があればクローズしてリスナーを外す（重複接続を防ぐ）
    if (ws) {
        try {
            ws.onopen = null;
            ws.onmessage = null;
            ws.onclose = null;
            ws.onerror = null;
            if (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING) {
                ws.close(1000, 'replaced');
            }
        } catch (e) {
            console.warn('Error while closing existing ws', e);
        }
        ws = null;
    }

    const protocol = location.protocol === "https:" ? "wss:" : "ws:";
    const basePath = getBasePath();
    const url = `${protocol}//${location.host}${basePath}/ws/replay?roomId=${encodeURIComponent(currentRoomId)}&clientType=${encodeURIComponent(currentClientType)}&vduNo=${encodeURIComponent(currentVduNo)}&clientId=${encodeURIComponent(currentClientId)}`;
    ws = new WebSocket(url);

    ws.onopen = () => {
        console.log("Replay WebSocket connected.");
        clearReconnectTimer();
        fetchCurrentState(currentClientType, currentVduNo, currentClientId);
    };

    ws.onmessage = (event) => {
        // メッセージはJSONを想定するため、パースエラーはキャッチしてログに出す
        try {
            const data = JSON.parse(event.data);
            renderState(data);
        } catch (e) {
            console.error("Replay WebSocket: invalid message received", e, event.data);
        }
    };

    ws.onclose = (event) => {
        console.warn("Replay WebSocket closed.", event && event.code, event && event.reason);
        if (event && event.code !== 1000) {
            scheduleReconnect();
        }
    };

    // 再接続としてwindow.addEventListener[focus][visibilitychange][pageshow]も検討
    ws.onerror = (error) => {
        console.error("Replay WebSocket error.", error);
    };
};

// ページアンロード時のクリーンアップ
window.addEventListener('beforeunload', () => {
    try {
        if (ws) {
            ws.close(1000, 'unload');
        }
    } catch (e) {
        // noop
    }
    clearReconnectTimer();
});

/**
 * 現在の状態をサーバーから取得する
 * @param {string} clientType - クライアントタイプ
 * @param {string|number} vduNo - VDU番号
 * @param {string} clientId - クライアントID
 */
const fetchCurrentState = (clientType, vduNo, clientId) => {
    const basePath = getBasePath();
    fetch(`${location.origin}${basePath}/replay/state?roomId=replayMode&clientType=${encodeURIComponent(clientType || "EVENT")}&vduNo=${encodeURIComponent(vduNo || 0)}&clientId=${encodeURIComponent(clientId || "")}`, { method: "GET" })
        .then((res) => {
            if (!res.ok) throw new Error("state fetch failed");
            return res.json();
        })
        .then((data) => renderState(data))
        .catch((e) => console.error(e));
};

/**
 * AVDUアラートをテーブルにレンダリングする
 * @param {Array} alerts - アラートデータの配列
 */
const renderAvduAlerts = (alerts) => {
    const tbody = getById("avduAlertBody");
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!alerts || !alerts.length) {
        const trEmpty = document.createElement("tr");
        const tdEmpty = document.createElement("td");
        tdEmpty.colSpan = 10;
        tdEmpty.textContent = "該当アラートなし";
        trEmpty.appendChild(tdEmpty);
        tbody.appendChild(trEmpty);
        return;
    }
    for (let i = 0; i < alerts.length; i++) {
        const a = alerts[i];
        const tr = document.createElement("tr");
        [a.alertId, a.unitNo, a.alertTag, a.alertName1, a.alertName2, a.alertSeverity, a.columnNo, a.firsthit, a.flick, a.yokokuColor].forEach((v) => {
            const td = document.createElement("td");
            td.textContent = (v === null || v === undefined || v === "") ? "-" : v;
            tr.appendChild(td);
        });
        tbody.appendChild(tr);
    }
};

/**
 * 状態データをUIに反映する
 * @param {Object} state - 状態データオブジェクト
 */
const renderState = (state) => {
    // state が falsy の場合に備える（呼び出し元が空を渡す可能性があるため）
    if (!state) {
        console.warn('renderState called with falsy state');
        state = {};
    }

    setText("currentOperator", state.controllerUserName || null);
    setText("currentOperatorIp", state.operatorIp || null);
    setText("playStatus", state.playStatus || null);
    setText("startDateTimeLabel", state.startDateTime || null);
    setText("currentReplayTimeLabel", state.currentReplayTime || null);
    setText("speedLabel", state.speed ? `${state.speed}x` : null);
    setText("lastCommandLabel", state.lastCommand || null);
    setText("unitNoLabel", state.unitNo || null);
    setText("replayModeLabel", state.replayMode || null);
    setText("currentPageIdLabel", state.lastPageId || null);
    setText("lastAppliedEventIdLabel", state.lastAppliedOperationId || null);
    setText("lastAppliedEventTypeLabel", state.lastAppliedActionType || null);
    setText("lastApplyResultLabel", state.lastApplyResult || null);
    setText("lastAppliedOccurredAtLabel", state.lastAppliedOccurredAt || null);
    setText("lastControlIdLabel", state.lastControlId || null);
    setText("lastButtonIdIdLabel", state.lastButtonId || null);
    setText("lastValueLabel", state.lastValue || null);
    applyOperateState(state);
    const frame = getById("replayFrame");
    if (frame && state.lastPageId) {
        const basePath = getBasePath();
        // selectedVduNo が無ければ unitNo を代替に使うなどのフォールバックを用意
        const vdu = (state.selectedVduNo !== undefined && state.selectedVduNo !== null) ? state.selectedVduNo : (state.unitNo !== undefined ? state.unitNo : 0);
        const nextUrl = `${window.location.origin}${basePath}/vdu/${vdu}/${state.lastPageId}.html`;
        if (frame.getAttribute("src") !== nextUrl) frame.setAttribute("src", nextUrl);
    }
    if (state && state.clientType === "AVDU") renderAvduAlerts(state.avduAlerts || []);
    if (window.renderEventFromState) {
        window.renderEventFromState(state);
    }
};

export { connect, fetchCurrentState, renderState };