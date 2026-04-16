package com.example.app.support.debug.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.example.app.support.debug.dto.DebugResetRequestDto;
import com.example.app.support.debug.dto.DebugResetResultDto;
import com.example.app.support.debug.service.DebugResetService;

/**
 * デバッグ用リセット実行サーブレットです。
 */
@WebServlet("/support/debug/reset")
public class DebugResetServlet extends HttpServlet {

    private DebugResetService debugResetService;

    @Override
    public void init() throws ServletException {
        this.debugResetService = new DebugResetService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/support/debug/reset.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        DebugResetRequestDto dto = new DebugResetRequestDto();
        dto.setResetType(request.getParameter("resetType"));

        DebugResetResultDto result = debugResetService.reset(dto);

        request.setAttribute("result", result);
        request.getRequestDispatcher("/support/debug/reset.jsp").forward(request, response);
    }
}
