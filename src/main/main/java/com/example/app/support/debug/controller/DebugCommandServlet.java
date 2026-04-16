package com.example.app.support.debug.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.example.app.support.debug.dto.DebugCommandRequestDto;
import com.example.app.support.debug.dto.DebugCommandResultDto;
import com.example.app.support.debug.service.DebugCommandService;

/**
 * デバッグコマンド実行用サーブレットです。
 */
@WebServlet("/support/debug/command")
public class DebugCommandServlet extends HttpServlet {

    private DebugCommandService debugCommandService;

    @Override
    public void init() throws ServletException {
        this.debugCommandService = new DebugCommandService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/support/debug/command.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        DebugCommandRequestDto dto = new DebugCommandRequestDto();
        dto.setCommandType(request.getParameter("commandType"));
        dto.setReplayTime(request.getParameter("replayTime"));
        dto.setSpeed(request.getParameter("speed"));
        dto.setTargetFrom(request.getParameter("targetFrom"));
        dto.setTargetTo(request.getParameter("targetTo"));

        DebugCommandResultDto result = debugCommandService.execute(dto);

        request.setAttribute("result", result);
        request.getRequestDispatcher("/support/debug/command.jsp").forward(request, response);
    }
}
