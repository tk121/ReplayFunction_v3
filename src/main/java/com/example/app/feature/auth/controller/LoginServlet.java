package com.example.app.feature.auth.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.dto.LoginRequest;
import com.example.app.feature.auth.dto.LoginResponse;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.dto.ErrorResponse;
import com.example.app.feature.replay.common.model.ReplayState;

@WebServlet("/ReplayFunction/replay/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            LoginRequest requestBody = JsonUtil.readValue(req.getReader(), LoginRequest.class);

            LoginUser loginUser = AppRuntime.getAuthModule()
                    .getAuthService()
                    .login(requestBody.getUserName(), requestBody.getPassword());

            HttpSession session = req.getSession(true);
            session.setAttribute("replay.loginUser", loginUser);

            ReplayState sharedReplayState = AppRuntime.getReplayModule()
                    .getReplaySessionService()
                    .getState("replayMode");

            AppRuntime.getReplayModule()
                    .getReplaySessionService()
                    .transferControlAtLogin(sharedReplayState, loginUser);

            LoginResponse response = new LoginResponse();
            response.setLoggedIn(true);
            response.setUserId(loginUser.getUserId());
            response.setUserName(loginUser.getUserName());
            response.setCanControl(loginUser.isCanControl());
            response.setControllerUserName(sharedReplayState.getControllerUserName());

            JsonUtil.writeValue(resp.getWriter(), response);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage()));
            } catch (Exception ignore) {
            }
        }
    }
}