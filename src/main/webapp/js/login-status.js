(function () {
    function getContextPath() {
        var pathName = window.location.pathname;
        var index = pathName.indexOf("/", 1);
        return index >= 0 ? pathName.substring(0, index) : "";
    }

    function refreshLoginStatus() {
        fetch(getContextPath() + "/login-status", {
            method: "GET",
            credentials: "include",
            cache: "no-store"
        })
        .then(function (response) {
            if (!response.ok) {
                throw new Error("status=" + response.status);
            }
            return response.json();
        })
        .then(function (data) {
            var loginUserCountEl = document.getElementById("loginUserCount");
            var onlineUserCountEl = document.getElementById("onlineUserCount");
            var maxLoginUsersEl = document.getElementById("maxLoginUsers");

            if (loginUserCountEl) {
                loginUserCountEl.textContent = data.loginUserCount;
            }
            if (onlineUserCountEl) {
                onlineUserCountEl.textContent = data.onlineUserCount;
            }
            if (maxLoginUsersEl) {
                maxLoginUsersEl.textContent = data.maxLoginUsers;
            }
        })
        .catch(function (error) {
            console.error("login-status refresh error:", error);
        });
    }

    refreshLoginStatus();
    setInterval(refreshLoginStatus, 5000);
})();

// menu.jsp などログイン後画面に追加する ping
//<script>
//(function () {
//    function sendPing() {
//        fetch("${pageContext.request.contextPath}/ping", {
//            method: "POST",
//            credentials: "include",
//            cache: "no-store"
//        }).catch(function (error) {
//            console.error("ping error:", error);
//        });
//    }
//
//    sendPing();
//    setInterval(sendPing, 10000);
//})();
//</script>