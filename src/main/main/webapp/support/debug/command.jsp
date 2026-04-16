<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.app.support.debug.dto.DebugCommandResultDto" %>
<%
    DebugCommandResultDto result = (DebugCommandResultDto) request.getAttribute("result");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Debug Command</title>
</head>
<body>
    <h1>Debug Command</h1>

    <form method="post" action="<%= request.getContextPath() %>/support/debug/command">
        <table>
            <tr>
                <th>Command Type</th>
                <td>
                    <select name="commandType">
                        <option value="PLAY">PLAY</option>
                        <option value="STOP">STOP</option>
                        <option value="GO_HEAD">GO_HEAD</option>
                        <option value="GO_TAIL">GO_TAIL</option>
                        <option value="SET_SPEED">SET_SPEED</option>
                        <option value="JUMP_TIME">JUMP_TIME</option>
                        <option value="APPLY_CONDITION">APPLY_CONDITION</option>
                    </select>
                </td>
            </tr>
            <tr>
                <th>Replay Time</th>
                <td><input type="text" name="replayTime" value=""></td>
            </tr>
            <tr>
                <th>Speed</th>
                <td><input type="text" name="speed" value=""></td>
            </tr>
            <tr>
                <th>Target From</th>
                <td><input type="text" name="targetFrom" value=""></td>
            </tr>
            <tr>
                <th>Target To</th>
                <td><input type="text" name="targetTo" value=""></td>
            </tr>
        </table>
        <input type="submit" value="Execute">
    </form>

    <% if (result != null) { %>
        <h2>Result</h2>
        <p>success = <%= result.isSuccess() %></p>
        <p>message = <%= result.getMessage() %></p>
    <% } %>

    <p><a href="<%= request.getContextPath() %>/support/debug/replay-state">replay-state</a></p>
</body>
</html>
