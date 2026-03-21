package com.example.app.feature.replay.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.dto.ErrorResponse;
import com.example.app.feature.replay.dto.ReplayHeartbeatRequest;
import com.example.app.feature.replay.dto.ReplayStateResponse;

/**
 * replay heartbeat API 用 Servlet です。
 *
 * <p>
 * 操作権保持者のブラウザから定期的に呼ばれ、
 * heartbeat 時刻を更新します。
 * </p>
 */
@WebServlet("/ReplayFunction_v3/replay/heartbeat")
public class ReplayHeartbeatServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            ReplayHeartbeatRequest requestBody = JsonUtil.readValue(req.getReader(), ReplayHeartbeatRequest.class);

            ReplayStateResponse response = AppRuntime.getReplayCoordinator().handleHeartbeat(
                    requestBody.getRoomId(),
                    requestBody.getClientId());

            JsonUtil.writeValue(resp.getWriter(), response);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage()));
            } catch (Exception ignore) {
                // 必要ならログ出力
            }
        }
    }
}
