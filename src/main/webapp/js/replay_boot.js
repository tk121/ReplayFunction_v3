(function () {
  if (window.REPLAY_SCREEN_TYPE === "VDU") {
    return;
  }

  window.REPLAY_SCREEN_TYPE = "CONTROL";

  var targetDate = document.getElementById("targetDate");
  var periodHours = document.getElementById("periodHours");
  var startHour = document.getElementById("startHour");
  var operatorName = document.getElementById("operatorName");

  var btnApply = document.getElementById("btnApply");
  var btnPlay = document.getElementById("btnPlay");
  var btnStop = document.getElementById("btnStop");
  var btnFastForward = document.getElementById("btnFastForward");
  var btnHead = document.getElementById("btnHead");
  var btnTail = document.getElementById("btnTail");
  
  var HEARTBEAT_INTERVAL_MILLIS = 5000;
  var clientId = getOrCreateClientId();

  /**
   * ブラウザ単位で保持する clientId を取得します。
   *
   * <p>
   * localStorage に保持することで、ページ再読み込み時も同じ clientId を使います。
   * </p>
   */
  function getOrCreateClientId() {
    var key = "replay.clientId";
    var existing = localStorage.getItem(key);
    if (existing && existing.length > 0) {
      return existing;
    }

    var value = "client-" + new Date().getTime() + "-" + Math.floor(Math.random() * 1000000);
    localStorage.setItem(key, value);
    return value;
  }


  function formatDate(date) {
    var y = date.getFullYear();
    var m = String(date.getMonth() + 1).padStart(2, "0");
    var d = String(date.getDate()).padStart(2, "0");
    return y + "-" + m + "-" + d;
  }

  function getAllowedHours(period) {
    if (period === "4") {
      return [0, 4, 8, 12, 16, 20];
    }
    if (period === "12") {
      return [0, 12];
    }
    return [0];
  }

  function rebuildStartHourOptions() {
    var currentValue = startHour.value;
    var hours = getAllowedHours(periodHours.value);

    startHour.innerHTML = "";
    for (var i = 0; i < hours.length; i++) {
      var opt = document.createElement("option");
      opt.value = String(hours[i]);
      opt.textContent = String(hours[i]).padStart(2, "0") + ":00";
      startHour.appendChild(opt);
    }

    if (hours.indexOf(Number(currentValue)) >= 0) {
      startHour.value = currentValue;
    } else {
      startHour.value = String(hours[0]);
    }
  }

  function getStartDateTime() {
    var date = targetDate.value;
    var hour = String(startHour.value).padStart(2, "0");
    return date + "T" + hour + ":00:00";
  }

  function getOperatorName() {
    var value = operatorName.value ? operatorName.value.trim() : "";
    if (!value) {
      alert("操作者名を入力してください。");
      operatorName.focus();
      throw new Error("operatorName is required");
    }
    return value;
  }
  
  /**
   * 操作者名を localStorage に保存します。
   *
   * <p>
   * 今後ログイン画面導入後は、この部分をログインユーザ表示へ差し替えやすくするためです。
   * </p>
   */
  function saveOperatorName() {
    var value = operatorName.value ? operatorName.value.trim() : "";
    if (value) {
      localStorage.setItem("replay.operatorName", value);
    }
  }

  function loadOperatorName() {
    var saved = localStorage.getItem("replay.operatorName");
    if (saved && !operatorName.value) {
      operatorName.value = saved;
    }
  }

  /**
   * control API を呼びます。
   */
  function sendControl(command) {
	saveOperatorName();
    var payload = {
      roomId: "global",
	  clientId: clientId,
      command: command,
      startDateTime: getStartDateTime(),
      periodHours: Number(periodHours.value),
      operatorName: getOperatorName()
    };

    return fetch("api/replay/control", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    }).then(function (res) {
      if (!res.ok) {
        return res.json().then(function (data) {
          throw new Error(data.message || "request failed");
        });
      }
      return res.json();
    }).then(function (data) {
      if (window.ReplayWs) {
        window.ReplayWs.renderState(data);
      }
      return data;
    });
  }
  
  /**
   * heartbeat を送信します。
   *
   * <p>
   * 操作権を持っていない場合に送っても、サーバ側で無視されます。
   * </p>
   */
  function sendHeartbeat() {
    fetch("api/replay/heartbeat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        roomId: "global",
        clientId: clientId
      })
    }).then(function (res) {
      if (!res.ok) {
        return;
      }
      return res.json();
    }).then(function (data) {
      if (data && window.ReplayWs) {
        window.ReplayWs.renderState(data);
      }
    }).catch(function (e) {
      console.error("heartbeat failed:", e);
    });
  }

  function startHeartbeatLoop() {
    setInterval(function () {
      sendHeartbeat();
    }, HEARTBEAT_INTERVAL_MILLIS);
  }

  function initDefault() {
    var now = new Date();
    var today = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
    targetDate.value = formatDate(today);
    periodHours.value = "4";
    rebuildStartHourOptions();
    startHour.value = "0";
	loadOperatorName();
  }

  function bindVduButtons() {
    var buttons = document.querySelectorAll(".btnOpenVdu");
    for (var i = 0; i < buttons.length; i++) {
      buttons[i].addEventListener("click", function () {
        var vduNo = this.getAttribute("data-vdu");
        window.open("vdu.html?vdu=" + encodeURIComponent(vduNo), "VDU" + vduNo, "width=1200,height=900");
      });
    }
  }

  periodHours.addEventListener("change", rebuildStartHourOptions);
  
  operatorName.addEventListener("change", saveOperatorName);
  operatorName.addEventListener("keyup", saveOperatorName);

  btnApply.addEventListener("click", function () {
    sendControl("APPLY_CONDITION").catch(function (e) {
      alert("条件反映に失敗しました: " + e.message);
    });
  });

  btnPlay.addEventListener("click", function () {
    sendControl("PLAY").catch(function (e) {
      alert("再生に失敗しました: " + e.message);
    });
  });

  btnStop.addEventListener("click", function () {
    sendControl("STOP").catch(function (e) {
      alert("停止に失敗しました: " + e.message);
    });
  });

  btnFastForward.addEventListener("click", function () {
    sendControl("FAST_FORWARD").catch(function (e) {
      alert("早送りに失敗しました: " + e.message);
    });
  });

  btnHead.addEventListener("click", function () {
    sendControl("GO_HEAD").catch(function (e) {
      alert("先頭移動に失敗しました: " + e.message);
    });
  });

  btnTail.addEventListener("click", function () {
    sendControl("GO_TAIL").catch(function (e) {
      alert("最後尾移動に失敗しました: " + e.message);
    });
  });

  initDefault();
  bindVduButtons();

  if (window.ReplayWs) {
    window.ReplayWs.connect("global", "CONTROL", "", clientId);
    window.ReplayWs.fetchCurrentState(0, clientId);
  }
})();