(function() {
    if (window.REPLAY_SCREEN_TYPE === "VDU" || window.REPLAY_SCREEN_TYPE === "AVDU") {
        return;
    }

    window.REPLAY_SCREEN_TYPE = "CONTROL";

    var targetDate = document.getElementById("targetDate");
    var periodHours = document.getElementById("periodHours");
	var startTime = document.getElementById("startTime");
	var startTimeRule = document.getElementById("startTimeRule");
    var operatorName = document.getElementById("operatorName");

    var btnApply = document.getElementById("btnApply");
    var btnPlay = document.getElementById("btnPlay");
    var btnStop = document.getElementById("btnStop");
    var btnFastForward = document.getElementById("btnFastForward");
    var btnHead = document.getElementById("btnHead");
    var btnTail = document.getElementById("btnTail");
    var btnOpenAvdu = document.getElementById("btnOpenAvdu");

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

	function updateStartTimeConstraint() {
	    var allowedHours = getAllowedHours(periodHours.value);
	    var current = startTime.value;

	    if (startTimeRule) {
	        if (periodHours.value === "4") {
	            startTimeRule.textContent = "4時間表示の場合は 00,04,08,12,16,20 時台を指定してください";
	        } else if (periodHours.value === "12") {
	            startTimeRule.textContent = "12時間表示の場合は 00,12 時台を指定してください";
	        } else {
	            startTimeRule.textContent = "24時間表示の場合は 00 時台を指定してください";
	        }
	    }

	    if (!current) {
	        startTime.value = String(allowedHours[0]).padStart(2, "0") + ":00";
	        return;
	    }

	    var parts = current.split(":");
	    if (parts.length < 2) {
	        startTime.value = String(allowedHours[0]).padStart(2, "0") + ":00";
	        return;
	    }

	    var hour = Number(parts[0]);
	    var minute = Number(parts[1]);

	    if (allowedHours.indexOf(hour) === -1) {
	        startTime.value = String(allowedHours[0]).padStart(2, "0") + ":" + String(isNaN(minute) ? 0 : minute).padStart(2, "0");
	    }
	}
	
	function validateStartTime() {
	    var value = startTime.value;
	    if (!value) {
	        alert("開始時刻を入力してください。");
	        startTime.focus();
	        throw new Error("startTime is required");
	    }

	    var parts = value.split(":");
	    if (parts.length < 2) {
	        alert("開始時刻の形式が不正です。");
	        startTime.focus();
	        throw new Error("invalid startTime format");
	    }

	    var hour = Number(parts[0]);
	    var minute = Number(parts[1]);

	    if (isNaN(hour) || isNaN(minute) || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
	        alert("開始時刻の値が不正です。");
	        startTime.focus();
	        throw new Error("invalid startTime value");
	    }

	    var allowedHours = getAllowedHours(periodHours.value);
	    if (allowedHours.indexOf(hour) === -1) {
	        alert("表示期間 " + periodHours.value + "時間 の場合、開始時刻は " + allowedHours.join(", ") + " 時台のみ指定できます。");
	        startTime.focus();
	        throw new Error("startTime hour is not allowed");
	    }

	    return value;
	}

	function getStartDateTime() {
	    var date = targetDate.value;
	    if (!date) {
	        alert("表示日を入力してください。");
	        targetDate.focus();
	        throw new Error("targetDate is required");
	    }

	    var time = validateStartTime(); // HH:mm
	    return date + "T" + time + ":00";
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
            roomId: "replayMode",
            clientId: clientId,
            command: command,
            startDateTime: getStartDateTime(),
            periodHours: Number(periodHours.value),
            operatorName: getOperatorName()
        };

        return fetch("ReplayFunction_v3/replay/control", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
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
        fetch("ReplayFunction_v3/replay/heartbeat", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                roomId: "replayMode",
                clientId: clientId
            })
        }).then(function(res) {
            if (!res.ok) {
                return;
            }
            return res.json();
        }).then(function(data) {
            if (data && window.ReplayWs) {
                window.ReplayWs.renderState(data);
            }
        }).catch(function(e) {
            console.error("heartbeat failed:", e);
        });
    }

    function startHeartbeatLoop() {
        setInterval(function() {
            sendHeartbeat();
        }, HEARTBEAT_INTERVAL_MILLIS);
    }

    function initDefault() {
		periodHours.value = "4";
		startTime.value = "00:00";
		updateStartTimeConstraint();
		loadOperatorName();
    }

    function bindVduButtons() {
        var buttons = document.querySelectorAll(".btnOpenVdu");
        for (var i = 0;i < buttons.length;i++) {
            buttons[i].addEventListener("click", function() {
                var vduNo = this.getAttribute("data-vdu");
                window.open("vdu.html?vdu=" + encodeURIComponent(vduNo), "VDU" + vduNo, "width=1200,height=900");
            });
        }
    }

    function bindAvduButton() {
        if (!btnOpenAvdu) {
            return;
        }
        btnOpenAvdu.addEventListener("click", function() {
            window.open("avdu.html", "AVDU", "width=1400,height=900");
        });
    }

	periodHours.addEventListener("change", updateStartTimeConstraint);

	startTime.addEventListener("change", function() {
	    try {
	        validateStartTime();
	    } catch (e) {
	        // エラー時は allowed の先頭へ補正
	        var allowedHours = getAllowedHours(periodHours.value);
	        startTime.value = String(allowedHours[0]).padStart(2, "0") + ":00";
	    }
	});

    operatorName.addEventListener("change", saveOperatorName);
    operatorName.addEventListener("keyup", saveOperatorName);

    btnApply.addEventListener("click", function() {
        sendControl("APPLY_CONDITION").catch(function(e) {
            alert("条件反映に失敗しました: " + e.message);
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

    btnFastForward.addEventListener("click", function() {
        sendControl("FAST_FORWARD").catch(function(e) {
            alert("早送りに失敗しました: " + e.message);
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

    initDefault();
    bindVduButtons();
    bindAvduButton();

    if (window.ReplayWs) {
        window.ReplayWs.connect("replayMode", "CONTROL", "", clientId);
        window.ReplayWs.fetchCurrentState("CONTROL", 0, clientId);
    }
//    startHeartbeatLoop();
})();