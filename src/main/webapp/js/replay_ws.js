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

  function connect(roomId, clientType, vduNo) {
    var protocol = location.protocol === "https:" ? "wss:" : "ws:";
    var basePath = location.pathname.substring(0, location.pathname.lastIndexOf("/"));
    var url = protocol + "//" + location.host + basePath + "/ws/replay"
      + "?roomId=" + encodeURIComponent(roomId || "global")
      + "&clientType=" + encodeURIComponent(clientType || "")
      + "&vduNo=" + encodeURIComponent(vduNo || "");

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

  function fetchCurrentState(vduNo) {
    fetch("api/replay/state?roomId=global&vduNo=" + encodeURIComponent(vduNo || 0), {
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

  function renderState(state) {
    setText("currentOperator", state.operatorName);
    setText("currentOperatorIp", state.operatorIp);
    setText("playStatus", state.playStatus);
    setText("startDateTimeLabel", state.startDateTime);
    setText("periodHoursLabel", state.periodHours ? state.periodHours + "時間" : "-");
    setText("currentReplayTimeLabel", state.currentReplayTime);
    setText("speedLabel", state.speed ? state.speed + "x" : "-");
    setText("lastCommandLabel", state.lastCommand);
    setText("displayUrlLabel", state.displayUrl);
    setText("currentPageIdLabel", state.currentPageId);
    setText("lastAppliedEventIdLabel", state.lastAppliedEventId);
    setText("lastAppliedEventTypeLabel", state.lastAppliedEventType);
    setText("lastApplyResultLabel", state.lastApplyResult);
    setText("lastAppliedOccurredAtLabel", state.lastAppliedOccurredAt);
    setText("lastControlIdLabel", state.lastControlId);
    setText("lastSymbolIdLabel", state.lastSymbolId);
    setText("lastValueLabel", state.lastValue);

    var frame = getById("replayFrame");
    if (frame && state.displayUrl) {
      if (frame.getAttribute("src") !== state.displayUrl) {
        frame.setAttribute("src", state.displayUrl);
      }
    }
  }

  return {
    connect: connect,
    fetchCurrentState: fetchCurrentState,
    renderState: renderState
  };
})();