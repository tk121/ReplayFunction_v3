package com.example.app.feature.replay.event.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.common.dto.ErrorResponse;
import com.example.app.feature.replay.event.dto.ReplayEventResponse;

@WebServlet("/ReplayFunction/replay/event")
public class ReplayEventServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            String roomId = req.getParameter("roomId");
            ReplayEventResponse response = AppRuntime.getReplayEventService().getCurrentSharedSeries(roomId);
            JsonUtil.writeValue(resp.getWriter(), response.getSeries());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage()));
            } catch (Exception ignore) {
            }
        }
    }
}