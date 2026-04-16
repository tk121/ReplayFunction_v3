package com.example.app.support.debug.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.app.support.debug.dto.DebugReplayStateDto;
import com.example.app.support.debug.service.DebugReplayStateService;

/**
 * リプレイ状態表示用サーブレットです。
 */
@WebServlet("/support/debug/replay-state")
public class DebugReplayStateServlet extends HttpServlet {

    private DebugReplayStateService debugReplayStateService;

    @Override
    public void init() throws ServletException {
        this.debugReplayStateService = new DebugReplayStateService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DebugReplayStateDto dto = debugReplayStateService.getReplayState();
        request.setAttribute("dto", dto);
        request.getRequestDispatcher("/support/debug/replay-state.jsp").forward(request, response);
    }
}
