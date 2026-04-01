package com.example.app.feature.auth.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.model.ReplayState;

@WebServlet("/ReplayFunction/replay/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session != null) {
            LoginUser loginUser = (LoginUser) session.getAttribute("replay.loginUser");

            if (loginUser != null) {
                ReplayState state = AppRuntime.getReplaySessionService().getState("replayMode");
                synchronized (state) {
                    if (loginUser.getUserId().equals(state.getControllerUserId())) {
                        state.setControllerUserId(null);
                        state.setControllerUserName(null);
                    }
                }
            }

            session.invalidate();
        }

        localRedirect(resp, req.getContextPath() + "/login.html");
    }

    private void localRedirect(HttpServletResponse resp, String url) throws IOException {
        resp.sendRedirect(url);
    }
}
