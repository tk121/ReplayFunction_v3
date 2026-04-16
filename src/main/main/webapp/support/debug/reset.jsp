<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.app.support.debug.dto.DebugResetResultDto" %>
<%
    DebugResetResultDto result = (DebugResetResultDto) request.getAttribute("result");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Debug Reset</title>
</head>
<body>
    <h1>Debug Reset</h1>

    <form method="post" action="<%= request.getContextPath() %>/support/debug/reset">
        <table>
            <tr>
                <th>Reset Type</th>
                <td>
                    <select name="resetType">
                        <option value="REPLAY_STATE">REPLAY_STATE</option>
                        <option value="CACHE_ONLY">CACHE_ONLY</option>
                        <option value="WS_SESSION_ONLY">WS_SESSION_ONLY</option>
                        <option value="ALL">ALL</option>
                    </select>
                </td>
            </tr>
        </table>
        <input type="submit" value="Reset">
    </form>

    <% if (result != null) { %>
        <h2>Result</h2>
        <p>success = <%= result.isSuccess() %></p>
        <p>message = <%= result.getMessage() %></p>
    <% } %>

    <p><a href="<%= request.getContextPath() %>/support/debug/replay-state">replay-state</a></p>
</body>
</html>
