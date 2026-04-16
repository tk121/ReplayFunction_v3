<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.app.support.debug.dto.DebugHealthDto" %>
<%
    DebugHealthDto dto = (DebugHealthDto) request.getAttribute("dto");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Debug Health</title>
</head>
<body>
    <h1>Debug Health</h1>

    <table border="1">
        <tr><th>App Status</th><td><%= dto.getAppStatus() %></td></tr>
        <tr><th>Database Status</th><td><%= dto.getDatabaseStatus() %></td></tr>
        <tr><th>Replay Engine Status</th><td><%= dto.getReplayEngineStatus() %></td></tr>
        <tr><th>WsHub Status</th><td><%= dto.getWsHubStatus() %></td></tr>
        <tr><th>Message</th><td><%= dto.getMessage() %></td></tr>
    </table>

    <p><a href="<%= request.getContextPath() %>/support/debug/replay-state">replay-state</a></p>
</body>
</html>
