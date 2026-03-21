package com.example.app.feature.replay.controller.ws;

import java.net.URI;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.model.ReplayState;

/**
 * replay 用 WebSocket Endpoint です。
 *
 * <p>
 * 表示画面や操作画面からの WebSocket 接続を受け付け、
 * 接続直後に現在状態を返し、
 * 以後は WsHub 経由のブロードキャスト配信対象に登録します。
 * </p>
 */
@ServerEndpoint("/ws/replay")
public class ReplayWsEndpoint {

    /**
     * WebSocket 接続開始時に呼ばれます。
     *
     * <p>
     * クエリパラメータから roomId / clientType / vduNo を読み取り、
     * クライアント情報を登録します。
     * その後、そのクライアント向けの現在状態を1回送信します。
     * </p>
     *
     * @param session WebSocket セッション
     */
    @OnOpen
    public void onOpen(Session session) {
        try {
            String roomId = getQueryParam(session, "roomId");
            String clientType = getQueryParam(session, "clientType");
            int vduNo = parseInt(getQueryParam(session, "vduNo"), 0);
            String clientId = getQueryParam(session, "clientId");

            // roomId 未指定時は replayMode を使用
            if (roomId == null || roomId.length() == 0) {
                roomId = "replayMode";
            }

            WsClient client = new WsClient(session, roomId, clientType, vduNo, clientId);

            // 接続クライアントとして登録
            AppRuntime.getReplayWsHub().register(client);

            // 接続直後に現在状態を返す
            ReplayState state = AppRuntime.getReplaySessionService().getState(roomId);
            AppRuntime.getReplayWsHub().sendCurrentState(
                    client,
                    state,
                    AppRuntime.getReplayResponseService());
            
            // 初回接続時に、必要な現在スナップショットを準備
            AppRuntime.getReplayCoordinator().prepareCurrentDisplayState(state, clientType);

        } catch (Exception e) {
            try {
                session.close();
            } catch (Exception ignore) {
                // 必要ならログ出力
            }
        }
    }

    /**
     * WebSocket 切断時に呼ばれます。
     *
     * @param session WebSocket セッション
     */
    @OnClose
    public void onClose(Session session) {
        AppRuntime.getReplayWsHub().unregister(session);
    }

    /**
     * クエリパラメータ値を取得します。
     *
     * <p>
     * 例:
     * </p>
     * <pre>
     * /ws/replay?roomId=replayMode&clientType=VDU&vduNo=1
     * </pre>
     *
     * @param session WebSocket セッション
     * @param key 取得したいキー
     * @return 値。未存在時は null
     */
    private String getQueryParam(Session session, String key) {
        URI uri = session.getRequestURI();
        String qs = uri.getQuery();
        if (qs == null || qs.length() == 0) {
            return null;
        }

        String[] pairs = qs.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = pair.substring(0, idx);
                String v = pair.substring(idx + 1);
                if (key.equals(k)) {
                    return v;
                }
            }
        }
        return null;
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