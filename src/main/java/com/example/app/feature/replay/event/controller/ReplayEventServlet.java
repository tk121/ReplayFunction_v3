package com.example.app.feature.replay.event.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.event.dto.ReplayEventResponse;
import com.example.app.feature.replay.graphic.dto.ErrorResponse;

@WebServlet("/ReplayFunction_v3/replay/event/*")
public class ReplayEventServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final Logger log = LoggerFactory.getLogger(ReplayEventServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	
    		log.info("Received request: " + req.getRequestURI() + "?" + req.getQueryString());
    		
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        try {
            String roomId = req.getParameter("roomId");
            int displayHours = Integer.parseInt(req.getParameter("displayHours"));
            ReplayEventResponse response = AppRuntime.getReplayEventService().getEventSeries(roomId, displayHours);
            JsonUtil.writeValue(resp.getWriter(), response.getSeries());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try { JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage())); } catch (Exception ignore) {}
        }
        
    }
}
