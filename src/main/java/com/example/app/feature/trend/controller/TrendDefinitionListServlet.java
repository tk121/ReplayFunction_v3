package com.example.app.feature.trend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.dto.ErrorResponse;
import com.example.app.feature.trend.dto.TrendDefinitionListResponse;
import com.example.app.feature.trend.dto.TrendDefinitionResponse;
import com.example.app.feature.trend.model.TrendDefinition;

@WebServlet("/ReplayFunction/trend/definitions")
public class TrendDefinitionListServlet extends javax.servlet.http.HttpServlet {
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

            List<TrendDefinition> definitions = AppRuntime.getTrendModule()
                    .getTrendDefinitionService()
                    .findByUserId(loginUser.getUserId());

            TrendDefinitionListResponse response = new TrendDefinitionListResponse();
            response.setTrends(toResponseList(definitions));

            JsonUtil.writeValue(resp.getWriter(), response);

        } catch (Exception e) {
            resp.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
            try {
                JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage()));
            } catch (Exception ignore) {
            }
        }
    }

    private List<TrendDefinitionResponse> toResponseList(List<TrendDefinition> definitions) {
        List<TrendDefinitionResponse> result =
                new ArrayList<TrendDefinitionResponse>();

        if (definitions == null) {
            return result;
        }

        for (TrendDefinition definition : definitions) {
            TrendDefinitionResponse row = new TrendDefinitionResponse();
            row.setTrendId(definition.getTrendId());
            row.setTrendName(definition.getTrendName());
            row.setDeviceIds(new ArrayList<String>(definition.getDeviceIds()));
            result.add(row);
        }

        return result;
    }

    private LoginUser getLoginUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (LoginUser) session.getAttribute("replay.loginUser");
    }
}