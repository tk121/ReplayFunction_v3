<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.app.support.debug.dto.DebugReplayStateDto" %>
<%
    DebugReplayStateDto dto = (DebugReplayStateDto) request.getAttribute("dto");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Debug Replay State</title>
</head>
<body>
    <h1>Debug Replay State</h1>

    <table border="1">
        <tr><th>Replay Status</th><td><%= dto.getReplayStatus() %></td></tr>
        <tr><th>Current Replay Time</th><td><%= dto.getCurrentReplayTime() %></td></tr>
        <tr><th>Base Start Time</th><td><%= dto.getBaseStartTime() %></td></tr>
        <tr><th>Speed</th><td><%= dto.getSpeed() %></td></tr>
        <tr><th>Target From</th><td><%= dto.getTargetFrom() %></td></tr>
        <tr><th>Target To</th><td><%= dto.getTargetTo() %></td></tr>
        <tr><th>Operator User</th><td><%= dto.getOperatorUserId() %></td></tr>
        <tr><th>Connected Session Count</th><td><%= dto.getConnectedSessionCount() %></td></tr>
        <tr><th>Last Tick Time</th><td><%= dto.getLastTickTime() %></td></tr>
        <tr><th>Last Tick Duration(ms)</th><td><%= dto.getLastTickDurationMs() %></td></tr>
    </table>

    <p>
        <a href="<%= request.getContextPath() %>/support/debug/health">health</a>
        |
        <a href="<%= request.getContextPath() %>/support/debug/command">command</a>
        |
        <a href="<%= request.getContextPath() %>/support/debug/reset">reset</a>
    </p>
</body>
</html>
