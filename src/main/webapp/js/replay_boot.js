(function() {
    if (window.REPLAY_SCREEN_TYPE === "VDU" || window.REPLAY_SCREEN_TYPE === "AVDU") {
        return;
    }

    window.REPLAY_SCREEN_TYPE = "EVENT";

    var targetDate = document.getElementById("targetDate");
    var displayHours = document.getElementById("displayHours");
    var startTime = document.getElementById("startTime");
    var replayMode = document.getElementById("replayMode");
    var unitNo = document.getElementById("unitNo");
    var playSpeed = document.getElementById("playSpeed");
    var btnApply = document.getElementById("btnApply");
    var btnPlay = document.getElementById("btnPlay");
    var btnStop = document.getElementById("btnStop");
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

    function validateStartDateTime() {
        if (!targetDate.value) {
            throw new Error("表示日を入力してください。");
        }
        if (!startTime.value) {
            throw new Error("開始時刻を入力してください。");
        }
        return targetDate.value + "T" + startTime.value + ":00";
    }

    function validateSpeed() {
        var speed = Number(playSpeed.value);
        if (speed !== 1 && speed !== 2 && speed !== 4 && speed !== 8) {
            throw new Error("速度は 1, 2, 4, 8 のいずれかを選択してください。");
        }
        return speed;
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
            startDateTime: replayMode.value === "HISTORY" ? validateStartDateTime() : null,
            displayHours: Number(displayHours.value),
            unitNo: Number(unitNo.value),
            replayMode: replayMode.value,
            operatorName: user.userName,
            speed: command === "CHANGE_SPEED" ? validateSpeed() : null
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

            if (playSpeed && data && data.speed) {
                playSpeed.value = String(data.speed);
            }
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
        if (playSpeed) {
            playSpeed.value = "1";
        }
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
    });

    displayHours.addEventListener("change", function() {
        if (window.redrawAllCharts) {
            window.redrawAllCharts();
        }
    });

    playSpeed.addEventListener("change", function() {
        sendControl("CHANGE_SPEED").catch(function(e) {
            alert("速度変更に失敗しました: " + e.message);
        });
    });

    btnPlay.addEventListener("click", function() {
        sendControl("PLAY").catch(function(e) {
            alert("再生に失敗しました: " + e.message);
        });
    });

    btnStop.addEventListener("click", function() {
        sendControl("STOP").catch(function(e) {
            alert("停止に失敗しました: " + e.message);
        });
    });

    btnHead.addEventListener("click", function() {
        sendControl("GO_HEAD").catch(function(e) {
            alert("先頭移動に失敗しました: " + e.message);
        });
    });

    btnTail.addEventListener("click", function() {
        sendControl("GO_TAIL").catch(function(e) {
            alert("最後尾移動に失敗しました: " + e.message);
        });
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
        window.ReplayWs.connect("replayMode", "EVENT", "", clientId);
        window.ReplayWs.fetchCurrentState("EVENT", 0, clientId);
    }

    window.renderEventFromState = renderEventFromState;
})();