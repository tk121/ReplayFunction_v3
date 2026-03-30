window.ReplayWs = (function () {
  var ws = null;

  function getById(id) { return document.getElementById(id); }
  function setText(id, value) {
    var el = getById(id);
    if (el) el.textContent = (value === null || value === undefined || value === "") ? "-" : value;
    }
  function setDisabled(id, disabled) {
    var el = getById(id);
    if (el) el.disabled = !!disabled;
    }

  /**
   * 操作可否に応じて、操作ボタンをグレーアウトします。
   */
  function applyOperateState(state) {
    var isRealtime = state && state.replayMode === "REALTIME";
    var disabled = !state || !state.canOperate;
    setDisabled("btnApply", disabled);
    setDisabled("btnPlay", disabled || isRealtime);
    setDisabled("btnStop", disabled || isRealtime);
    setDisabled("btnFastForward", disabled || isRealtime);
    setDisabled("btnHead", disabled || isRealtime);
    setDisabled("btnTail", disabled || isRealtime);

    // 操作権保持者名を表示
    setText("currentControllerName", state ? state.controllerUserName : null);
    setText("currentUserName", state ? state.currentUserName : null);
    setText("currentUserRole", !state ? null : (state.loggedIn ? (state.currentUserCanControl ? "操作権保有可能" : "閲覧専用") : "未ログイン"));
    var msgEl = getById("canOperateMessage");
    if (msgEl) {
      if (!state) msgEl.textContent = "-";
      else if (state.canOperate) msgEl.textContent = "あなたは操作可能です。";
      else if (!state.loggedIn) msgEl.textContent = "未ログインのため操作できません。";
      else if (!state.currentUserCanControl) msgEl.textContent = "このユーザーは操作権を持てません。";
      else if (state.controllerUserName) msgEl.textContent = "現在は " + state.controllerUserName + " が操作中です。";
      else msgEl.textContent = "現在は操作できません。";
      }
    }

  function connect(roomId, clientType, vduNo, clientId, userId) {
    var protocol = location.protocol === "https:" ? "wss:" : "ws:";
    var basePath = location.pathname.substring(0, location.pathname.lastIndexOf("/"));
    var url = protocol + "//" + location.host + basePath + "/ws/replay"
      + "?roomId=" + encodeURIComponent(roomId || "replayMode")
      + "&clientType=" + encodeURIComponent(clientType || "")
      + "&vduNo=" + encodeURIComponent(vduNo || "")
      + "&clientId=" + encodeURIComponent(clientId || "")
      + "&userId=" + encodeURIComponent(userId || "");
    ws = new WebSocket(url);
    ws.onmessage = function (event) { renderState(JSON.parse(event.data)); };
  }

  function fetchCurrentState(clientType, vduNo, clientId) {
    fetch("ReplayFunction_v3/replay/state?roomId=replayMode"
      + "&clientType=" + encodeURIComponent(clientType || "CONTROL")
      + "&vduNo=" + encodeURIComponent(vduNo || 0)
      + "&clientId=" + encodeURIComponent(clientId || ""), { method: "GET" })
      .then(function (res) { if (!res.ok) throw new Error("state fetch failed"); return res.json(); })
      .then(function (data) { renderState(data); })
      .catch(function (e) { console.error(e); });
  }
  
  function renderAvduAlerts(alerts) {
    var tbody = getById("avduAlertBody");
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!alerts || !alerts.length) {
      var trEmpty = document.createElement("tr");
      var tdEmpty = document.createElement("td");
      tdEmpty.colSpan = 10;
      tdEmpty.textContent = "該当アラートなし";
      trEmpty.appendChild(tdEmpty);
      tbody.appendChild(trEmpty);
      return;
    }
    for (var i = 0; i < alerts.length; i++) {
      var a = alerts[i];
      var tr = document.createElement("tr");
      [a.alertId, a.unitNo, a.alertTag, a.alertName1, a.alertName2, a.alertSeverity, a.columnNo, a.firsthit, a.flick, a.yokokuColor].forEach(function(v){
        var td = document.createElement("td"); td.textContent = (v === null || v === undefined || v === "") ? "-" : v; tr.appendChild(td);
      });
      tbody.appendChild(tr);
    }
  }

  function renderState(state) {
    setText("currentOperator", state.operatorName);
    setText("currentOperatorIp", state.operatorIp);
    setText("playStatus", state.playStatus);
    setText("startDateTimeLabel", state.startDateTime);
    setText("currentReplayTimeLabel", state.currentReplayTime);
    setText("speedLabel", state.speed ? state.speed + "x" : "-");
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
    var frame = getById("replayFrame");
    if (frame && state.lastPageId) {
      var nextUrl = window.location.origin + "/ReplayFunction_v3/vdu/" + state.selectedVduNo + "/" + state.lastPageId + ".html";
      if (frame.getAttribute("src") !== nextUrl) frame.setAttribute("src", nextUrl);
	   }
    if (state && state.clientType === "AVDU") renderAvduAlerts(state.avduAlerts || []);
  }

  return { connect: connect, fetchCurrentState: fetchCurrentState, renderState: renderState };
})();
