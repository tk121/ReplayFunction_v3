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
 * 接続中クライアントを保持し、
 * 特定 room の現在状態を各クライアント向けに組み立てて送信します。
 * </p>
 *
 * <p>
 * このクラスは「送信専用」に寄せており、
 * DB取得や C 呼び出しは行いません。
 * </p>
 */
public class WsHub {

	/**
	 * 接続中クライアント一覧です。
	 *
	 * <p>
	 * key は WebSocket Session ID です。
	 * </p>
	 */
	private final Map<String, WsClient> clientMap = new ConcurrentHashMap<String, WsClient>();

	public void register(WsClient client) {
		clientMap.put(client.getSession().getId(), client);
	}

	public void unregister(Session session) {
		if (session != null)
			clientMap.remove(session.getId());
	}

	/**
	 * 指定クライアントへ現在状態を1回送信します。
	 *
	 * <p>
	 * WebSocket 接続直後に、そのクライアント向けの初期表示状態を返すために使用します。
	 * </p>
	 *
	 * @param client 送信先クライアント
	 * @param state replay 状態
	 * @param responseService レスポンス生成サービス
	 * @throws Exception 送信失敗時
	 */
	public void sendCurrentState(WsClient client, ReplayState state, ReplayResponseService responseService)
			throws Exception {
		ReplayStateResponse response = buildResponseForClient(client, state, responseService);
		client.getSession().getBasicRemote().sendText(JsonUtil.writeValueAsString(response));
	}

	/**
	 * 指定 state を、その room に属する全クライアントへ配信します。
	 *
	 * <p>
	 * 各クライアントの vduNo に応じて個別レスポンスを作成して送信します。
	 * </p>
	 *
	 * @param state 配信対象状態
	 * @param responseService レスポンス生成サービス
	 * @throws Exception 送信失敗時
	 */
	public void broadcast(ReplayState state, ReplayResponseService responseService) throws Exception {
		for (WsClient client : clientMap.values()) {
			if (client == null || client.getSession() == null || !client.getSession().isOpen())
				continue;
			if (!safeEquals(state.getRoomId(), client.getRoomId()))
				continue;
			try {
				ReplayStateResponse response = buildResponseForClient(client, state, responseService);
				client.getSession().getBasicRemote().sendText(JsonUtil.writeValueAsString(response));
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
			loginUser = AppRuntime.getReplayAuthService().findLoginUserByUserId(client.getUserId());
		}

		if (client.isAvdu()) {
			return responseService.buildAvduResponse(state, loginUser);
		}
		if (client.isVdu()) {
			return responseService.buildVduResponse(state, client.getVduNo(), loginUser);
		}
		return responseService.buildControlResponse(state, loginUser);
	}

	/**
	 * null 安全に文字列比較を行います。
	 *
	 * @param a 比較対象1
	 * @param b 比較対象2
	 * @return 等しい場合 true
	 */
	private boolean safeEquals(String a, String b) {
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}
}