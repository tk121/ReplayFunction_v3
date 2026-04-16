<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Debug Menu</title>
</head>
<body>
    <h1>Debug Menu</h1>
    <ul>
        <li><a href="${pageContext.request.contextPath}/support/debug/replay-state">Replay State</a></li>
        <li><a href="${pageContext.request.contextPath}/support/debug/health">Health</a></li>
        <li><a href="${pageContext.request.contextPath}/support/debug/command">Command</a></li>
        <li><a href="${pageContext.request.contextPath}/support/debug/reset">Reset</a></li>
    </ul>
</body>
</html>
