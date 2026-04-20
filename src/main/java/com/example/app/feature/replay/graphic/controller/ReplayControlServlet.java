package com.example.app.feature.replay.graphic.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.dto.ErrorResponse;
import com.example.app.feature.replay.graphic.dto.ReplayControlRequest;
import com.example.app.feature.replay.graphic.dto.ReplayStateResponse;

@WebServlet("/ReplayFunction/replay/control")
public class ReplayControlServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ReplayControlServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        log.info("Received control request: {}", req.getRequestURI());

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            ReplayControlRequest requestBody =
                    JsonUtil.readValue(req.getReader(), ReplayControlRequest.class);

            String remoteIp = getClientIp(req);
            LoginUser loginUser = getLoginUser(req.getSession(false));

            ReplayStateResponse response = AppRuntime.getReplayModule()
                    .getReplayCoordinator()
                    .handleControl(requestBody, remoteIp, loginUser);

            JsonUtil.writeValue(resp.getWriter(), response);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage()));
            } catch (Exception ignore) {
            }
        }
    }

    private LoginUser getLoginUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (LoginUser) session.getAttribute("replay.loginUser");
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && xff.trim().length() > 0) {
            int idx = xff.indexOf(',');
            return idx >= 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        return req.getRemoteAddr();
    }
}