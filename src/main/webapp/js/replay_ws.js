window.ReplayWs = (function () {
  var ws = null;

  function getById(id) {
    return document.getElementById(id);
  }

  function setText(id, value) {
    var el = getById(id);
    if (el) {
      el.textContent = (value === null || value === undefined || value === "") ? "-" : value;
    }
  }
  
  function setDisabled(id, disabled) {
    var el = getById(id);
    if (el) {
      el.disabled = !!disabled;
    }
  }

  /**
   * 操作可否に応じて、操作ボタンをグレーアウトします。
   */
  function applyOperateState(state) {
    var disabled = !state || !state.canOperate;

    setDisabled("btnApply", disabled);
    setDisabled("btnPlay", disabled);
    setDisabled("btnStop", disabled);
    setDisabled("btnFastForward", disabled);
    setDisabled("btnHead", disabled);
    setDisabled("btnTail", disabled);

    // 操作権保持者名を表示
    setText("currentControllerName", state ? state.controllerUserName : null);

    var msgEl = getById("canOperateMessage");
    if (msgEl) {
      if (!state) {
        msgEl.textContent = "-";
      } else if (state.canOperate) {
        msgEl.textContent = "あなたは操作可能です。";
      } else if (state.controllerUserName) {
        msgEl.textContent = "現在は " + state.controllerUserName + " が操作中です。";
      } else {
        msgEl.textContent = "現在は操作できません。";
      }
    }
  }

  function connect(roomId, clientType, vduNo, clientId) {
    var protocol = location.protocol === "https:" ? "wss:" : "ws:";
    var basePath = location.pathname.substring(0, location.pathname.lastIndexOf("/"));
    var url = protocol + "//" + location.host + basePath + "/ws/replay"
      + "?roomId=" + encodeURIComponent(roomId || "replayMode")
      + "&clientType=" + encodeURIComponent(clientType || "")
      + "&vduNo=" + encodeURIComponent(vduNo || "")
	        + "&clientId=" + encodeURIComponent(clientId || "");

    ws = new WebSocket(url);

    ws.onopen = function () {
      console.log("WebSocket connected:", url);
    };

    ws.onmessage = function (event) {
      var data = JSON.parse(event.data);
      renderState(data);
    };

    ws.onclose = function () {
      console.log("WebSocket closed");
    };
  }

  /**
   * 現在状態を HTTP で再取得します。
   */
  function fetchCurrentState(clientType, vduNo, clientId) {
    fetch("ReplayFunction_v3/replay/state?roomId=replayMode"
      + "&clientType=" + encodeURIComponent(clientType || "CONTROL")
      + "&vduNo=" + encodeURIComponent(vduNo || 0)
      + "&clientId=" + encodeURIComponent(clientId || ""), {
      method: "GET"
    }).then(function (res) {
      if (!res.ok) {
        throw new Error("state fetch failed");
      }
      return res.json();
    }).then(function (data) {
      renderState(data);
    }).catch(function (e) {
      console.error(e);
    });
  }
  
  function renderAvduAlerts(alerts) {
    var tbody = getById("avduAlertBody");
    if (!tbody) {
      return;
    }

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

      appendCell(tr, a.alertId);
      appendCell(tr, a.unitNo);
      appendCell(tr, a.alertTag);
      appendCell(tr, a.alertName1);
      appendCell(tr, a.alertName2);
      appendCell(tr, a.alertSeverity);
      appendCell(tr, a.columnNo);
      appendCell(tr, a.firsthit);
      appendCell(tr, a.flick);
      appendCell(tr, a.yokokuColor);

      tbody.appendChild(tr);
    }
  }

  function appendCell(tr, value) {
    var td = document.createElement("td");
    td.textContent = (value === null || value === undefined || value === "") ? "-" : value;
    tr.appendChild(td);
  }

  /**
   * 受信した状態を画面へ反映します。
   */
  function renderState(state) {
    setText("currentOperator", state.operatorName);
    setText("currentOperatorIp", state.operatorIp);
    setText("playStatus", state.playStatus);
    setText("startDateTimeLabel", state.startDateTime);
    setText("periodHoursLabel", state.periodHours ? state.periodHours + "時間" : "-");
    setText("currentReplayTimeLabel", state.currentReplayTime);
    setText("speedLabel", state.speed ? state.speed + "x" : "-");
    setText("lastCommandLabel", state.lastCommand);
    setText("currentPageIdLabel", state.lastPageId);
    setText("lastAppliedEventIdLabel", state.lastAppliedOperationId);
    setText("lastAppliedEventTypeLabel", state.lastAppliedActionType);
    setText("lastApplyResultLabel", state.lastApplyResult);
    setText("lastAppliedOccurredAtLabel", state.lastAppliedOccurredAt);
    setText("lastControlIdLabel", state.lastControlId);
    setText("lastButtonIdIdLabel", state.lastButtonId);
    setText("lastValueLabel", state.lastValue);

	// 操作可否のUI反映
	applyOperateState(state);

	// VDU画面では iframe の表示URLも更新
    var frame = getById("replayFrame");
    if (frame && state.lastPageId) {
		
		var nextUrl = window.location.origin + "/ReplayFunction_v3/vdu/" + state.selectedVduNo + "/" +state.lastPageId + ".html";
		console.log("Next URL: " + nextUrl);
      if (frame.getAttribute("src") !== nextUrl) {
        frame.setAttribute("src", nextUrl);
      }
    }
	
	if (state && state.clientType === "AVDU") {
	     renderAvduAlerts(state.avduAlerts || []);
	   }
  }

  return {
    connect: connect,
    fetchCurrentState: fetchCurrentState,
    renderState: renderState
  };
})();