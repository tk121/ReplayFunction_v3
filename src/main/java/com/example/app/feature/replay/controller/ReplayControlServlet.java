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
import com.example.app.feature.replay.dto.ReplayControlRequest;
import com.example.app.feature.replay.dto.ReplayStateResponse;

/**
 * replay 制御API用 Servlet です。
 *
 * <p>
 * 操作画面から送られてきたコマンドを受け付け、
 * ReplayCoordinator に処理を委譲します。
 * </p>
 */
@WebServlet("/api/replay/control")
public class ReplayControlServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * POST リクエストを処理します。
     *
     * <p>
     * リクエストボディの JSON を ReplayControlRequest に変換し、
     * 再生制御を実行した結果を JSON で返します。
     * </p>
     *
     * @param req HTTPリクエスト
     * @param resp HTTPレスポンス
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            // 受信JSONを DTO に変換
            ReplayControlRequest requestBody = JsonUtil.readValue(req.getReader(), ReplayControlRequest.class);

            // クライアントIPを取得
            String remoteIp = getClientIp(req);

            // replay 制御を実行
            ReplayStateResponse response = AppRuntime.getReplayCoordinator().handleControl(requestBody, remoteIp);

            // 成功結果を JSON で返す
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
     * クライアントIPを取得します。
     *
     * <p>
     * リバースプロキシ配下を考慮し、まず X-Forwarded-For を見て、
     * なければ request の remoteAddr を使います。
     * </p>
     *
     * @param req HTTPリクエスト
     * @return クライアントIP
     */
    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && xff.trim().length() > 0) {
            int idx = xff.indexOf(',');
            return idx >= 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        return req.getRemoteAddr();
    }
}