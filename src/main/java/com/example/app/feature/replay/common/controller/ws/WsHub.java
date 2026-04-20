package com.example.app.feature.replay.common.controller.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import com.example.app.common.json.JsonUtil;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.model.ReplayState;
import com.example.app.feature.replay.common.service.ReplayResponseService;
import com.example.app.feature.replay.graphic.dto.ReplayStateResponse;

/**
 * WebSocket クライアントの管理と配信を担当するハブです。
 *
 * <p>
 * replay の shared state を、接続中クライアントごとに適切な DTO に変換して送信します。
 * </p>
 */
public class WsHub {

    private final Map<String, WsClient> clientMap =
            new ConcurrentHashMap<String, WsClient>();

    public void register(WsClient client) {
        clientMap.put(client.getSession().getId(), client);
    }

    public void unregister(Session session) {
        if (session != null) {
            clientMap.remove(session.getId());
        }
    }

    public void sendCurrentState(
            WsClient client,
            ReplayState state,
            ReplayResponseService responseService) throws Exception {

        ReplayStateResponse response =
                buildResponseForClient(client, state, responseService);

        client.getSession().getBasicRemote()
                .sendText(JsonUtil.writeValueAsString(response));
    }

    public void broadcast(
            ReplayState state,
            ReplayResponseService responseService) throws Exception {

        for (WsClient client : clientMap.values()) {
            if (client == null || client.getSession() == null || !client.getSession().isOpen()) {
                continue;
            }
            if (!safeEquals(state.getRoomId(), client.getRoomId())) {
                continue;
            }

            try {
                ReplayStateResponse response =
                        buildResponseForClient(client, state, responseService);

                client.getSession().getBasicRemote()
                        .sendText(JsonUtil.writeValueAsString(response));

            } catch (Exception e) {
                // 必要ならログ出力
            }
        }
    }

    private ReplayStateResponse buildResponseForClient(
            WsClient client,
            ReplayState state,
            ReplayResponseService responseService) throws Exception {

        LoginUser loginUser = null;
        if (client.getUserId() != null && client.getUserId().length() > 0) {
            loginUser = AppRuntime.getAuthModule()
                    .getAuthService()
                    .findLoginUserByUserId(client.getUserId());
        }

        if (client.isAvdu()) {
            return responseService.buildAvduResponse(state, loginUser);
        }
        if (client.isVdu()) {
            return responseService.buildVduResponse(state, client.getVduNo(), loginUser);
        }
        return responseService.buildControlResponse(state, loginUser);
    }

    private boolean safeEquals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}