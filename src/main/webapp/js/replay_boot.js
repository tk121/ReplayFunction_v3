/**
 * Replay boot module
 * リプレイ機能のメインUIを初期化し、イベント処理を行う。
 */
import { connect, fetchCurrentState, renderState } from './replay_ws.js';

(function() {
    // VDUまたはAVDU画面の場合は何もしない
    if (window.REPLAY_SCREEN_TYPE === "VDU" || window.REPLAY_SCREEN_TYPE === "AVDU") {
        return;
    }

    // 画面タイプを設定
    window.REPLAY_SCREEN_TYPE = "EVENT";

    // DOM要素の取得
    const targetDate = document.getElementById("targetDate");
    const displayHours = document.getElementById("displayHours");
    const startTime = document.getElementById("startTime");
    const replayMode = document.getElementById("replayMode");
    const unitNo = document.getElementById("unitNo");
    const playSpeed = document.getElementById("playSpeed");
    const btnApply = document.getElementById("btnApply");
    const btnPlay = document.getElementById("btnPlay");
    const btnStop = document.getElementById("btnStop");
    const btnHead = document.getElementById("btnHead");
    const btnTail = document.getElementById("btnTail");
    const btnOpenAvdu = document.getElementById("btnOpenAvdu");
    const btnLogout = document.getElementById("btnLogout");
    const loginUserName = document.getElementById("loginUserName");

    // クライアントIDの生成または取得
    const clientId = getOrCreateClientId();

    /**
     * クライアントIDを生成または取得する
     * localStorageに保存されている場合はそれを使用、ない場合は新規生成
     * @returns {string} クライアントID
     */
    function getOrCreateClientId() {
        const key = "replay.clientId";
        const existing = localStorage.getItem(key);
        if (existing && existing.length > 0) return existing;
        const value = `client-${new Date().getTime()}-${Math.floor(Math.random() * 1000000)}`;
        localStorage.setItem(key, value);
        return value;
    }

    /**
     * ログイン中のユーザ情報を取得する
     * @returns {Object|null} ユーザ情報オブジェクト、またはnull
     */
    function getStoredUser() {
        const raw = localStorage.getItem("replay.loginUser");
        return raw ? JSON.parse(raw) : null;
    }

    /**
     * ログイン中のユーザ情報をクリアする
     */
    function clearStoredUser() {
        localStorage.removeItem("replay.loginUser");
    }

    /**
     * ログインユーザー名の表示を更新する
     */
    function updateLoginUserDisplay() {
        const user = getStoredUser();
        if (loginUserName) {
            loginUserName.textContent = user && user.userName ? user.userName : "-";
        }
    }

    /**
     * 開始日時を検証する
     * @returns {string} ISO形式の日時文字列
     * @throws {Error} 入力が不正な場合
     */
    function validateStartDateTime() {
        if (!targetDate.value) {
            throw new Error("表示日を入力してください。");
        }
        if (!startTime.value) {
            throw new Error("開始時刻を入力してください。");
        }
        return `${targetDate.value}T${startTime.value}:00`;
    }

    /**
     * 再生速度を検証する
     * @returns {number} 検証済みの速度値
     * @throws {Error} 速度が不正な場合
     */
    function validateSpeed() {
        const speed = Number(playSpeed.value);
        if (speed !== 1 && speed !== 2 && speed !== 4 && speed !== 8) {
            throw new Error("速度は 1, 2, 4, 8 のいずれかを選択してください。");
        }
        return speed;
    }

    /**
     * コントロールコマンドをサーバーに送信する
     * @param {string} command - コマンド名
     * @returns {Promise} レスポンスのPromise
     */
    function sendControl(command) {
        const user = getStoredUser();
        if (!user) {
            location.href = "login.html";
            return Promise.reject(new Error("not logged in"));
        }

        const payload = {
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
        }).then((res) => {
            if (!res.ok) {
                return res.json().then((data) => {
                    throw new Error(data.message || "request failed");
                });
            }
            return res.json();
        }).then((data) => {
            renderState(data);
            renderEventFromState(data);

            if (playSpeed && data && data.speed) {
                playSpeed.value = String(data.speed);
            }
            return data;
        });
    }

    /**
     * イベントデータをJSONエリアに表示する
     * @param {Object} state - 状態データ
     */
    function renderEventFromState(state) {
        const area = document.getElementById("eventJsonArea");
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

    /**
     * ログアウト処理を行う
     */
    function logout() {
        fetch("ReplayFunction/replay/logout", {
            method: "POST"
        }).finally(() => {
            clearStoredUser();
            location.href = "login.html";
        });
    }

    /**
     * デフォルト値を設定する
     */
    function initDefault() {
        displayHours.value = "4";
        startTime.value = "00:00";
        if (playSpeed) {
            playSpeed.value = "1";
        }
        updateLoginUserDisplay();
    }

    /**
     * VDUボタンのイベントリスナーを設定する
     */
    function bindVduButtons() {
        const buttons = document.querySelectorAll(".btnOpenVdu");
        for (let i = 0; i < buttons.length; i++) {
            buttons[i].addEventListener("click", function() {
                const vduNo = this.getAttribute("data-vdu");
                window.open(`vdu.html?vdu=${encodeURIComponent(vduNo)}`, `VDU${vduNo}`, "width=1200,height=900");
            });
        }
    }

    /**
     * AVDUボタンのイベントリスナーを設定する
     */
    function bindAvduButton() {
        if (!btnOpenAvdu) return;
        btnOpenAvdu.addEventListener("click", function() {
            window.open("avdu.html", "AVDU", "width=1400,height=900");
        });
    }

    // イベントリスナーの設定
    replayMode.addEventListener("change", function() {
        const realtime = replayMode.value === "REALTIME";
        targetDate.disabled = realtime;
        startTime.disabled = realtime;
    });

    displayHours.addEventListener("change", function() {
        if (window.redrawAllCharts) {
            window.redrawAllCharts();
        }
    });

    playSpeed.addEventListener("change", function() {
        sendControl("CHANGE_SPEED").catch((e) => {
            alert(`速度変更に失敗しました: ${e.message}`);
        });
    });

    btnPlay.addEventListener("click", function() {
        sendControl("PLAY").catch((e) => {
            alert(`再生に失敗しました: ${e.message}`);
        });
    });

    btnStop.addEventListener("click", function() {
        sendControl("STOP").catch((e) => {
            alert(`停止に失敗しました: ${e.message}`);
        });
    });

    btnHead.addEventListener("click", function() {
        sendControl("GO_HEAD").catch((e) => {
            alert(`先頭移動に失敗しました: ${e.message}`);
        });
    });

    btnTail.addEventListener("click", function() {
        sendControl("GO_TAIL").catch((e) => {
            alert(`最後尾移動に失敗しました: ${e.message}`);
        });
    });

    btnApply.addEventListener("click", function() {
        sendControl("APPLY_CONDITION").catch((e) => {
            alert(`条件反映に失敗しました: ${e.message}`);
        });
    });

    if (btnLogout) {
        btnLogout.addEventListener("click", logout);
    }

    // 初期化処理
    initDefault();
    bindVduButtons();
    bindAvduButton();

    // WebSocket接続
    connect("replayMode", "EVENT", "", clientId);
    fetchCurrentState("EVENT", 0, clientId);

    // グローバル関数として公開
    window.renderEventFromState = renderEventFromState;
})();