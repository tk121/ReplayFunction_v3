package com.example.app.feature.auth.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.app.common.runtime.AppRuntime;

public class PingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userIdObj = session.getAttribute("loginUserId");
            if (userIdObj != null) {
                String userId = userIdObj.toString();
                AppRuntime.getAuthModule().getOnlineRegistry().touch(userId);
            }
        }

        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"result\":\"ok\"}");
    }
}
