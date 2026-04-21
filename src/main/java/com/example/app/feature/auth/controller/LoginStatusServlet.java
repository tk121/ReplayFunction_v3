package com.example.app.feature.auth.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.app.common.runtime.AppRuntime;

public class LoginStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AppRuntime runtime = AppRuntime.getInstance();

        int loginUserCount = runtime.getLoginUserRegistry().getLoginUserCount();
        int onlineUserCount = runtime.getOnlineUserRegistry().getOnlineUserCount();
        int maxLoginUsers = runtime.getLoginPolicy().getMaxLoginUsers();

        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(
                "{"
                + "\"loginUserCount\":" + loginUserCount + ","
                + "\"onlineUserCount\":" + onlineUserCount + ","
                + "\"maxLoginUsers\":" + maxLoginUsers
                + "}");
    }
}
