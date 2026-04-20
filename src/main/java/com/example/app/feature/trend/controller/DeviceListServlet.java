package com.example.app.feature.trend.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.dto.ErrorResponse;

@WebServlet("/ReplayFunction/trend/devices")
public class DeviceListServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest req,
                         javax.servlet.http.HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            LoginUser loginUser = getLoginUser(req.getSession(false));
            if (loginUser == null) {
                throw new IllegalStateException("ログインしていません");
            }

            List<String> devices = AppRuntime.getTrendModule()
                    .getDeviceService()
                    .findAllDeviceIds();

            JsonUtil.writeValue(resp.getWriter(), devices);

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