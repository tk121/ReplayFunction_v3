package com.example.app.support.debug.guard;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * support.debug へのアクセス制御フィルタです。
 * 現状は enabled=false のとき 404 を返す最小構成です。
 */
public class DebugAccessFilter implements Filter {

    private DebugEnabledChecker checker;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.checker = new DebugEnabledChecker();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!checker.isEnabled()) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
