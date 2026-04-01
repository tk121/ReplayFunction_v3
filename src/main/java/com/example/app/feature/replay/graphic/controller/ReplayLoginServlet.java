package com.example.app.feature.replay.graphic.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.common.auth.LoginUser;
import com.example.app.feature.replay.graphic.dto.ErrorResponse;
import com.example.app.feature.replay.graphic.dto.ReplayLoginRequest;
import com.example.app.feature.replay.graphic.dto.ReplayLoginResponse;

@WebServlet("/ReplayFunction/replay/login")
public class ReplayLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        try {
            ReplayLoginRequest requestBody = JsonUtil.readValue(req.getReader(), ReplayLoginRequest.class);
            LoginUser loginUser = AppRuntime.getReplayAuthService().login(requestBody.getUserName());
            HttpSession session = req.getSession(true);
            session.setAttribute("replay.loginUser", loginUser);
            AppRuntime.getReplaySessionService().transferControlAtLogin(AppRuntime.getReplaySessionService().getState("replayMode"), loginUser);
            ReplayLoginResponse response = new ReplayLoginResponse();
            response.setLoggedIn(true);
            response.setUserId(loginUser.getUserId());
            response.setUserName(loginUser.getUserName());
            response.setCanControl(loginUser.isCanControl());
            response.setControllerUserName(AppRuntime.getReplaySessionService().getState("replayMode").getControllerUserName());
            JsonUtil.writeValue(resp.getWriter(), response);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try { JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage())); } catch (Exception ignore) {}
        }
    }
}
