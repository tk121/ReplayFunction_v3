/**
 * WebSocket接続と状態管理モジュール
 * リプレイ機能のWebSocket通信を管理し、UIの状態更新を行う。
 */

// WebSocket接続インスタンス
let ws = null;
// 再接続タイマー
let reconnectTimer = null;
//                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   現在のルームID
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

    const protocol = location.protocol === "https:" ? "wss:" : "ws:";
    const basePath = location.pathname.substring(0, location.pathname.lastIndexOf("/"));
    const url = `${protocol}//${location.host}${basePath}/ws/replay?roomId=${encodeURIComponent(currentRoomId)}&clientType=${encodeURIComponent(currentClientType)}&vduNo=${encodeURIComponent(currentVduNo)}&clientId=${encodeURIComponent(currentClientId)}`;
    ws = new WebSocket(url);

    ws.onopen = () => {
        console.log("Replay WebSocket connected.");
        clearReconnectTimer();
        fetchCurrentState(currentClientType, currentVduNo, currentClientId);
    };

    ws.onmessage = (event) => {
        renderState(JSON.parse(event.data));
    };

    ws.onclose = (event) => {
        console.warn("Replay WebSocket closed.", event.code, event.reason);
        if (event.code !== 1000) {
            scheduleReconnect();
        }
    };

    // 再接続としてwindow.addEventListener[focus][visibilitychange][pageshow]も検討
    ws.onerror = (error) => {
        console.error("Replay WebSocket error.", error);
    };
};

/**
 * 現在の状態をサーバーから取得する
 * @param {string} clientType - クライアントタイプ
 * @param {string|number} vduNo - VDU番号
 * @param {string} clientId - クライアントID
 */
const fetchCurrentState = (clientType, vduNo, clientId) => {
    fetch(`ReplayFunction/replay/state?roomId=replayMode&clientType=${encodeURIComponent(clientType || "EVENT")}&vduNo=${encodeURIComponent(vduNo || 0)}&clientId=${encodeURIComponent(clientId || "")}`, { method: "GET" })
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
    setText("currentOperator", state.controllerUserName);
    setText("currentOperatorIp", state.operatorIp);
    setText("playStatus", state.playStatus);
    setText("startDateTimeLabel", state.startDateTime);
    setText("currentReplayTimeLabel", state.currentReplayTime);
    setText("speedLabel", state.speed ? `${state.speed}x` : "-");
    setText("lastCommandLabel", state.lastCommand);
    setText("unitNoLabel", state.unitNo);
    setText("replayModeLabel", state.replayMode);
    setText("currentPageIdLabel", state.lastPageId);
    setText("lastAppliedEventIdLabel", state.lastAppliedOperationId);
    setText("lastAppliedEventTypeLabel", state.lastAppliedActionType);
    setText("lastApplyResultLabel", state.lastApplyResult);
    setText("lastAppliedOccurredAtLabel", state.lastAppliedOccurredAt);
    setText("lastControlIdLabel", state.lastControlId);
    setText("lastButtonIdIdLabel", state.lastButtonId);
    setText("lastValueLabel", state.lastValue);
    applyOperateState(state);
    const frame = getById("replayFrame");
    if (frame && state.lastPageId) {
        const nextUrl = `${window.location.origin}/ReplayFunction/vdu/${state.selectedVduNo}/${state.lastPageId}.html`;
        if (frame.getAttribute("src") !== nextUrl) frame.setAttribute("src", nextUrl);
    }
    if (state && state.clientType === "AVDU") renderAvduAlerts(state.avduAlerts || []);
    if (window.renderEventFromState) {
        window.renderEventFromState(state);
    }
};

export { connect, fetchCurrentState, renderState };