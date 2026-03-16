package com.example.app.feature.replay.controller.ws;

import javax.websocket.Session;

/**
 * WebSocket 接続中クライアントの情報を保持するクラスです。
 *
 * <p>
 * どの Session が、どの roomId / clientType / vduNo に対応するかを
 * WsHub で管理するために使用します。
 * </p>
 */
public class WsClient {

	/** WebSocket セッション */
	private final Session session;

	/** 所属ルームID */
	private final String roomId;

	/**
	 * クライアント種別です。
	 *
	 * <p>
	 * 例:
	 * </p>
	 * <ul>
	 *   <li>CONTROL: 操作画面</li>
	 *   <li>VDU: 表示画面</li>
	 * </ul>
	 */
	private final String clientType;

	/**
	 * VDU番号です。
	 *
	 * <p>
	 * 表示画面の場合、どの VDU の状態を受け取るかを表します。
	 * CONTROL 側では 0 の想定です。
	 * </p>
	 */
	private final int vduNo;

	/**
	 * クライアント識別IDです。
	 *
	 * <p>
	 * 操作画面ではこの値を使って canOperate を個別判定します。
	 * </p>
	 */
	private final String clientId;

	/**
	 * コンストラクタです。
	 *
	 * @param session WebSocket セッション
	 * @param roomId ルームID
	 * @param clientType クライアント種別
	 * @param vduNo VDU番号
	 */
	public WsClient(Session session, String roomId, String clientType, int vduNo, String clientId) {
		this.session = session;
		this.roomId = roomId;
		this.clientType = clientType;
		this.vduNo = vduNo;
		this.clientId = clientId;
	}

	public Session getSession() {
		return session;
	}

	public String getRoomId() {
		return roomId;
	}

	public String getClientType() {
		return clientType;
	}

	public int getVduNo() {
		return vduNo;
	}
	
    public String getClientId() {
        return clientId;
    }
}