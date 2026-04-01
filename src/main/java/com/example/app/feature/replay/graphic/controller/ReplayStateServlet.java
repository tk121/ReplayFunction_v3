package com.example.app.feature.replay.graphic.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.common.auth.LoginUser;
import com.example.app.feature.replay.graphic.dto.ErrorResponse;
import com.example.app.feature.replay.graphic.dto.ReplayStateResponse;

/**
 * replay 現在状態取得API用 Servlet です。
 *
 * <p>
 * 指定された roomId / vduNo の現在状態を返します。
 * 主に画面初期表示時や、必要に応じた状態再取得で使用します。
 * </p>
 */
@WebServlet("/ReplayFunction/replay/state")
public class ReplayStateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final Logger log = LoggerFactory.getLogger(ReplayStateServlet.class);

    /**
     * GET リクエストを処理します。
     *
     * @param req HTTPリクエスト
     * @param resp HTTPレスポンス
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	
    		log.info("Received state request: " + req.getRequestURI() + "?" + req.getQueryString());
    		
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        try {
            String roomId = req.getParameter("roomId");
            String clientType = req.getParameter("clientType");
            int vduNo = parseInt(req.getParameter("vduNo"), 0);
            LoginUser loginUser = getLoginUser(req.getSession(false));
            ReplayStateResponse response = AppRuntime.getReplayCoordinator().getState(roomId, clientType, vduNo, loginUser);
            JsonUtil.writeValue(resp.getWriter(), response);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try { JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage())); } catch (Exception ignore) {}
            }
        }

    private LoginUser getLoginUser(HttpSession session) {
        if (session == null) return null;
        return (LoginUser) session.getAttribute("replay.loginUser");
    }

    /**
     * 文字列を int に変換します。
     *
     * @param value 変換元文字列
     * @param defaultValue 変換失敗時の既定値
     * @return int値
     */
    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
