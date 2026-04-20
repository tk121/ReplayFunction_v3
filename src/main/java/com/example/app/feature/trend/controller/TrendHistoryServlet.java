package com.example.app.feature.trend.controller;

import java.io.IOException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.dto.ErrorResponse;
import com.example.app.feature.trend.dto.TrendHistoryResponse;

@WebServlet("/ReplayFunction/trend/history")
public class TrendHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            LoginUser loginUser = getLoginUser(req.getSession(false));
            if (loginUser == null) {
                throw new IllegalStateException("ログインしていません");
            }

            Long trendId = parseLong(req.getParameter("trendId"));
            LocalDate targetDate = parseDate(req.getParameter("targetDate"));

            TrendHistoryResponse response = AppRuntime.getTrendModule()
                    .getTrendHistoryService()
                    .loadOneDay(trendId, targetDate, loginUser.getUserId());

            JsonUtil.writeValue(resp.getWriter(), response);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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

    private Long parseLong(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return Long.valueOf(value.trim());
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }
}
