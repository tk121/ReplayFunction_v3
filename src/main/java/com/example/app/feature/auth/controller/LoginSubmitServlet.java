package com.example.app.feature.auth.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.dto.LoginUserDto;
import com.example.app.feature.auth.session.LoginPolicy;
import com.example.app.feature.auth.session.LoginUserRegistry;
import com.example.app.feature.auth.session.OnlineUserRegistry;

public class LoginSubmitServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String userId = trim(request.getParameter("userId"));
        String password = trim(request.getParameter("password"));

        AppRuntime runtime = AppRuntime.getInstance();

        // ここは既存 AuthModule に合わせて置換
        LoginUserDto loginUser = runtime.getAuthModule().authenticate(userId, password);

        if (loginUser == null) {
            setCounts(request, runtime);
            request.setAttribute("errorMessage", "ユーザIDまたはパスワードが正しくありません。");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);

        LoginPolicy loginPolicy = runtime.getLoginPolicy();
        LoginUserRegistry loginUserRegistry = runtime.getLoginUserRegistry();
        OnlineUserRegistry onlineUserRegistry = runtime.getOnlineUserRegistry();

        boolean ok = loginUserRegistry.tryLogin(
                loginUser.getUserId(),
                session.getId(),
                loginPolicy.getMaxLoginUsers());

        if (!ok) {
            setCounts(request, runtime);
            request.setAttribute("errorMessage", "現在、ログイン可能人数の上限に達しています。");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        session.setAttribute("loginUser", loginUser);
        session.setAttribute("loginUserId", loginUser.getUserId());

        // ログイン直後はオンライン扱い
        onlineUserRegistry.touch(loginUser.getUserId());

        response.sendRedirect(request.getContextPath() + "/menu");
    }

    private void setCounts(HttpServletRequest request, AppRuntime runtime) {
        request.setAttribute("loginUserCount", runtime.getLoginUserRegistry().getLoginUserCount());
        request.setAttribute("onlineUserCount", runtime.getOnlineUserRegistry().getOnlineUserCount());
        request.setAttribute("maxLoginUsers", runtime.getLoginPolicy().getMaxLoginUsers());
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}