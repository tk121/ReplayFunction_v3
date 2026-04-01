package com.example.app.feature.auth.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.app.feature.auth.model.LoginUser;

@WebFilter(urlPatterns = {
        "/index.html",
        "/vdu.html",
        "/avdu.html",
        "/ReplayFunction/replay/state",
        "/ReplayFunction/replay/control",
        "/ReplayFunction/replay/event",
        "/ReplayFunction/replay/logout"
})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初期化処理なし
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        LoginUser loginUser = null;
        if (session != null) {
            loginUser = (LoginUser) session.getAttribute("replay.loginUser");
        }

        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 終了処理なし
    }
}
