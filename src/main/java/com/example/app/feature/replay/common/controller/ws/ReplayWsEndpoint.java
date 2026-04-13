package com.example.app.feature.replay.common.controller.ws;

import java.net.URI;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.model.ReplayState;

/**
 * replay 用 WebSocket Endpoint です。
 *
 * <p>
 * 表示画面や操作画面からの WebSocket 接続を受け付け、
 * 接続直後に現在状態を返し、
 * 以後は WsHub 経由のブロードキャスト配信対象に登録します。
 * </p>
 */
@ServerEndpoint(value = "/ws/replay", configurator = HttpSessionConfigurator.class)
public class ReplayWsEndpoint {

	private static final Logger log = LoggerFactory.getLogger(ReplayWsEndpoint.class);

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		try {

			log.info("WebSocket opened. sessionId);={}, query={}",
					session.getId(),
					session.getRequestURI() != null ? session.getRequestURI().getQuery() : null);

			String roomId = getQueryParam(session, "roomId");
			String clientType = getQueryParam(session, "clientType");
			int vduNo = parseInt(getQueryParam(session, "vduNo"), 0);
			String clientId = getQueryParam(session, "clientId");

			if (roomId == null || roomId.length() == 0) {
				roomId = "replayMode";
			}

			HttpSession httpSession = (HttpSession) config.getUserProperties()
					.get(HttpSessionConfigurator.HTTP_SESSION_KEY);

			LoginUser loginUser = null;
			if (httpSession != null) {
				loginUser = (LoginUser) httpSession.getAttribute("replay.loginUser");
			}

			// 未ログインなら接続を拒否
			if (loginUser == null) {
				session.close();
				return;
			}

			WsClient client = new WsClient(
					session,
					roomId,
					clientType,
					vduNo,
					clientId,
					loginUser.getUserId());

			AppRuntime.getReplayWsHub().register(client);

			ReplayState state = AppRuntime.getReplaySessionService().getState(roomId);
			AppRuntime.getReplayCoordinator().prepareDisplayStateForClient(state, clientType);
			AppRuntime.getReplayWsHub().sendCurrentState(client, state, AppRuntime.getReplayResponseService());

		} catch (Exception e) {
			try {
				session.close();
			} catch (Exception ignore) {
			}
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		log.warn("WebSocket closed. sessionId={}, code={}, reason={}",
				session.getId(),
				reason != null ? reason.getCloseCode() : null,
				reason != null ? reason.getReasonPhrase() : null);
		AppRuntime.getReplayWsHub().unregister(session);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		log.error("WebSocket error. sessionId={}",
				session != null ? session.getId() : "null",
				throwable);

		if (session != null) {
			AppRuntime.getReplayWsHub().unregister(session);
		}
	}

	private String getQueryParam(Session session, String key) {
		URI uri = session.getRequestURI();
		String qs = uri.getQuery();
		if (qs == null || qs.length() == 0)
			return null;

		String[] pairs = qs.split("&");
		for (int i = 0; i < pairs.length; i++) {
			String pair = pairs[i];
			int idx = pair.indexOf('=');
			if (idx > 0) {
				String k = pair.substring(0, idx);
				String v = pair.substring(idx + 1);
				if (key.equals(k))
					return v;
			}
		}
		return null;
	}

	private int parseInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}