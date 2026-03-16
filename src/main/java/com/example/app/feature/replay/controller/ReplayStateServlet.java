package com.example.app.feature.replay.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.dto.ErrorResponse;
import com.example.app.feature.replay.dto.ReplayStateResponse;

/**
 * replay 現在状態取得API用 Servlet です。
 *
 * <p>
 * 指定された roomId / vduNo の現在状態を返します。
 * 主に画面初期表示時や、必要に応じた状態再取得で使用します。
 * </p>
 */
@WebServlet("/api/replay/state")
public class ReplayStateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            String roomId = req.getParameter("roomId");
            int vduNo = parseInt(req.getParameter("vduNo"), 0);

            // この clientId をもとに canOperate を返す
            String clientId = req.getParameter("clientId");

            ReplayStateResponse response = AppRuntime.getReplayCoordinator().getState(roomId, vduNo, clientId);

            // JSON で返却
            JsonUtil.writeValue(resp.getWriter(), response);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                JsonUtil.writeValue(resp.getWriter(), new ErrorResponse(e.getMessage()));
            } catch (Exception ignore) {
                // 必要ならログ出力
            }
        }
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