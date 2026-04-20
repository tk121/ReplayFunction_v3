package com.example.app.feature.trend.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.dto.ErrorResponse;
import com.example.app.feature.trend.dto.TrendRealtimeUnsubscribeRequest;

@WebServlet("/ReplayFunction/trend/realtime/unsubscribe")
public class TrendRealtimeUnsubscribeServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(javax.servlet.http.HttpServletRequest req,
                          javax.servlet.http.HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            LoginUser loginUser = getLoginUser(req.getSession(false));
            if (loginUser == null) {
                throw new IllegalStateException("ログインしていません");
            }

            TrendRealtimeUnsubscribeRequest requestBody =
                    JsonUtil.readValue(req.getReader(), TrendRealtimeUnsubscribeRequest.class);

            if (requestBody.getClientId() == null || requestBody.getClientId().trim().length() == 0) {
                throw new IllegalArgumentException("clientId は必須です");
            }

            AppRuntime.getTrendModule()
                    .getTrendRealtimeSessionService()
                    .unsubscribe(loginUser.getUserId(), requestBody.getClientId());

            resp.getWriter().write("{\"unsubscribed\":true}");

        } catch (Exception e) {
            resp.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
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
}
