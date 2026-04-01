(function() {
    if (window.REPLAY_SCREEN_TYPE === "VDU" || window.REPLAY_SCREEN_TYPE === "AVDU") {
        return;
    }

    window.REPLAY_SCREEN_TYPE = "CONTROL";

    var targetDate = document.getElementById("targetDate");
    var displayHours = document.getElementById("displayHours");
    var startTime = document.getElementById("startTime");
    var startTimeRule = document.getElementById("startTimeRule");
    var replayMode = document.getElementById("replayMode");
    var unitNo = document.getElementById("unitNo");
    var btnApply = document.getElementById("btnApply");
    var btnPlay = document.getElementById("btnPlay");
    var btnStop = document.getElementById("btnStop");
    var btnFastForward = document.getElementById("btnFastForward");
    var btnHead = document.getElementById("btnHead");
    var btnTail = document.getElementById("btnTail");
    var btnOpenAvdu = document.getElementById("btnOpenAvdu");
    var btnLogout = document.getElementById("btnLogout");
    var loginUserName = document.getElementById("loginUserName");

    var clientId = getOrCreateClientId();

    function getOrCreateClientId() {
        var key = "replay.clientId";
        var existing = localStorage.getItem(key);
        if (existing && existing.length > 0) return existing;
        var value = "client-" + new Date().getTime() + "-" + Math.floor(Math.random() * 1000000);
        localStorage.setItem(key, value);
        return value;
    }

    function getStoredUser() {
        var raw = localStorage.getItem("replay.loginUser");
        return raw ? JSON.parse(raw) : null;
    }

    function clearStoredUser() {
        localStorage.removeItem("replay.loginUser");
    }

    function updateLoginUserDisplay() {
        var user = getStoredUser();
        if (loginUserName) {
            loginUserName.textContent = user && user.userName ? user.userName : "-";
        }
    }

    function getAllowedHours(period) {
        if (period === "4") return [0, 4, 8, 12, 16, 20];
        if (period === "12") return [0, 12];
        return [0];
    }

    function updateStartTimeConstraint() {
        var allowedHours = getAllowedHours(displayHours.value);
        var current = startTime.value;

        if (startTimeRule) {
            startTimeRule.textContent = displayHours.value === "4"
                ? "4時間表示の場合は 00,04,08,12,16,20 時台を指定してください"
                : (displayHours.value === "12"
                    ? "12時間表示の場合は 00,12 時台を指定してください"
                    : "24時間表示の場合は 00 時台を指定してください");
        }

        if (!current) {
            startTime.value = String(allowedHours[0]).padStart(2, "0") + ":00";
            return;
        }

        var parts = current.split(":");
        var hour = Number(parts[0]);
        var minute = Number(parts[1]);

        if (allowedHours.indexOf(hour) === -1) {
            startTime.value = String(allowedHours[0]).padStart(2, "0") + ":" + String(isNaN(minute) ? 0 : minute).padStart(2, "0");
        }
    }

    function validateStartTime() {
        var value = startTime.value;
        if (!value) throw new Error("開始時刻を入力してください。");

        var parts = value.split(":");
        var hour = Number(parts[0]);
        var allowedHours = getAllowedHours(displayHours.value);

        if (allowedHours.indexOf(hour) === -1) {
            throw new Error("表示期間に対応しない開始時刻です。");
        }

        return targetDate.value + "T" + value + ":00";
    }

    function sendControl(command) {
        var user = getStoredUser();
        if (!user) {
            location.href = "login.html";
            return Promise.reject(new Error("not logged in"));
        }

        var payload = {
            roomId: "replayMode",
            clientId: clientId,
            command: command,
            startDateTime: replayMode.value === "HISTORY" ? validateStartTime() : null,
            displayHours: Number(displayHours.value),
            unitNo: Number(unitNo.value),
            replayMode: replayMode.value,
            operatorName: user.userName
        };

        return fetch("ReplayFunction/replay/control", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        }).then(function(res) {
            if (!res.ok) {
                return res.json().then(function(data) {
                    throw new Error(data.message || "request failed");
                });
            }
            return res.json();
        }).then(function(data) {
            if (window.ReplayWs) {
                window.ReplayWs.renderState(data);
            }
            renderEventFromState(data);
            return data;
        });
    }

    function renderEventFromState(state) {
        var area = document.getElementById("eventJsonArea");
        if (!area) return;

        if (!state || !state.conditionApplied) {
            area.textContent = "まだ条件反映されていません。";
            if (window.onTrendDataReceived) {
                window.onTrendDataReceived({});
            }
            return;
        }

        area.textContent = JSON.stringify(state.eventSeries || {}, null, 2);

        if (window.onTrendDataReceived) {
            window.onTrendDataReceived(state.eventSeries || {});
        }
    }

    function logout() {
        fetch("ReplayFunction/replay/logout", {
            method: "POST"
        }).finally(function() {
            clearStoredUser();
            location.href = "login.html";
        });
    }

    function initDefault() {
        displayHours.value = "4";
        startTime.value = "00:00";
        updateStartTimeConstraint();
        updateLoginUserDisplay();
    }

    function bindVduButtons() {
        var buttons = document.querySelectorAll(".btnOpenVdu");
        for (var i = 0; i < buttons.length; i++) {
            buttons[i].addEventListener("click", function() {
                var vduNo = this.getAttribute("data-vdu");
                window.open("vdu.html?vdu=" + encodeURIComponent(vduNo), "VDU" + vduNo, "width=1200,height=900");
            });
        }
    }

    function bindAvduButton() {
        if (!btnOpenAvdu) return;
        btnOpenAvdu.addEventListener("click", function() {
            window.open("avdu.html", "AVDU", "width=1400,height=900");
        });
    }

    replayMode.addEventListener("change", function() {
        var realtime = replayMode.value === "REALTIME";
        targetDate.disabled = realtime;
        startTime.disabled = realtime;
        if (startTimeRule) startTimeRule.style.display = realtime ? "none" : "block";
    });

    displayHours.addEventListener("change", function() {
        updateStartTimeConstraint();
        if (window.redrawAllCharts) {
            window.redrawAllCharts();
        }
    });

    btnPlay.addEventListener("click", function() {
        sendControl("PLAY").catch(function(e) { alert("再生に失敗しました: " + e.message); });
    });

    btnStop.addEventListener("click", function() {
        sendControl("STOP").catch(function(e) { alert("停止に失敗しました: " + e.message); });
    });

    btnFastForward.addEventListener("click", function() {
        sendControl("FAST_FORWARD").catch(function(e) { alert("早送りに失敗しました: " + e.message); });
    });

    btnHead.addEventListener("click", function() {
        sendControl("GO_HEAD").catch(function(e) { alert("先頭移動に失敗しました: " + e.message); });
    });

    btnTail.addEventListener("click", function() {
        sendControl("GO_TAIL").catch(function(e) { alert("最後尾移動に失敗しました: " + e.message); });
    });

    btnApply.addEventListener("click", function() {
        sendControl("APPLY_CONDITION").catch(function(e) {
            alert("条件反映に失敗しました: " + e.message);
        });
    });

    if (btnLogout) {
        btnLogout.addEventListener("click", logout);
    }

    initDefault();
    bindVduButtons();
    bindAvduButton();

    if (window.ReplayWs) {
        var user = getStoredUser();
        window.ReplayWs.connect("replayMode", "CONTROL", "", clientId);
        window.ReplayWs.fetchCurrentState("CONTROL", 0, clientId);
    }

    window.renderEventFromState = renderEventFromState;
})();