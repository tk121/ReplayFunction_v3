package com.example.app.feature.trend.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.dto.ErrorResponse;
import com.example.app.feature.trend.dto.TrendDefinitionResponse;
import com.example.app.feature.trend.dto.TrendSaveRequest;
import com.example.app.feature.trend.model.TrendDefinition;

@WebServlet("/ReplayFunction/trend/save")
public class TrendSaveServlet extends javax.servlet.http.HttpServlet {
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

            TrendSaveRequest requestBody =
                    JsonUtil.readValue(req.getReader(), TrendSaveRequest.class);

            TrendDefinition saved = AppRuntime.getTrendModule()
                    .getTrendDefinitionService()
                    .save(loginUser.getUserId(), requestBody);

            TrendDefinitionResponse response = new TrendDefinitionResponse();
            response.setTrendId(saved.getTrendId());
            response.setTrendName(saved.getTrendName());
            response.setDeviceIds(saved.getDeviceIds());

            JsonUtil.writeValue(resp.getWriter(), response);

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