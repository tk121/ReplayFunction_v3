package com.example.app.support.debug.controller;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.example.app.support.debug.dto.DebugHealthDto;
import com.example.app.support.debug.service.DebugHealthService;

/**
 * 簡易ヘルスチェック表示用サーブレットです。
 */
@WebServlet("/support/debug/health")
public class DebugHealthServlet extends HttpServlet {

    private DebugHealthService debugHealthService;

    @Override
    public void init() throws ServletException {
        try {
            InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc/mydb");
            this.debugHealthService = new DebugHealthService(dataSource);
        } catch (Exception e) {
            throw new ServletException("Failed to initialize DebugHealthServlet.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DebugHealthDto dto = debugHealthService.getHealth();
        request.setAttribute("dto", dto);
        request.getRequestDispatcher("/support/debug/health.jsp").forward(request, response);
    }
}
