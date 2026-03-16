package com.example.app.feature.replay.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.app.feature.replay.dto.ReplayControlRequest;
import com.example.app.feature.replay.model.ReplayState;

/**
 * ReplayState の管理を担当するサービスです。
 *
 * <p>
 * room ごとの状態生成・取得・日時変換・基本入力チェックなど、
 * 状態管理に関する共通処理をまとめています。
 * </p>
 */
public class ReplaySessionService {

	/** 画面・API間で共通利用する日時フォーマット */
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	/** roomId ごとの状態保持マップ */
	private final Map<String, ReplayState> stateMap = new ConcurrentHashMap<String, ReplayState>();
	
    /** 操作権制御設定 */
    private final ReplayControlConfig controlConfig;

    public ReplaySessionService(ReplayControlConfig controlConfig) {
        this.controlConfig = controlConfig;
    }

	/**
	 * 指定 roomId の状態を取得します。
	 *
	 * <p>
	 * まだ存在しない場合はデフォルト状態を作成して返します。
	 * </p>
	 *
	 * @param roomId ルームID
	 * @return ReplayState
	 */
	public ReplayState getOrCreate(String roomId) {
		String normalized = normalizeRoomId(roomId);

		ReplayState current = stateMap.get(normalized);
		if (current != null) {
			return current;
		}

		ReplayState created = createDefault(normalized);
		ReplayState old = stateMap.putIfAbsent(normalized, created);
		return old != null ? old : created;
	}

	/**
	 * roomId の状態を返します。
	 *
	 * <p>
	 * 実装上は getOrCreate と同じです。
	 * </p>
	 *
	 * @param roomId ルームID
	 * @return ReplayState
	 */
	public ReplayState getState(String roomId) {
		return getOrCreate(roomId);
	}

	/**
	 * 全ルームの状態を返します。
	 *
	 * <p>
	 * ReplayEngine が定期的に全状態を巡回するときに使用します。
	 * </p>
	 *
	 * @return 全状態一覧
	 */
	public Collection<ReplayState> getAllStates() {
		return new ArrayList<ReplayState>(stateMap.values());
	}

	/**
	 * リクエスト基本項目を state に反映します。
	 *
	 * <p>
	 * 操作権が確定した後に呼ぶ想定です。
	 * </p>
	 *
	 * @param state 対象状態
	 * @param req リクエスト
	 * @param remoteIp 操作者IP
	 */
	public void applyBaseFields(ReplayState state, ReplayControlRequest req, String remoteIp) {
		synchronized (state) {
			state.setOperatorName(trimToNull(req.getOperatorName()));
			state.setOperatorIp(remoteIp);
			state.setStartDateTime(req.getStartDateTime());
			state.setPeriodHours(req.getPeriodHours().intValue());
		}
	}
	
	/**
	 * リクエスト内容を検証します。
	 *
	 * <p>
	 * 必須項目の未設定や、許可されない表示期間などをチェックします。
	 * </p>
	 *
	 * @param req リクエスト
	 */
    public void validateControlRequest(ReplayControlRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("リクエストがありません");
        }

        if (isBlank(req.getClientId())) {
            throw new IllegalArgumentException("clientId は必須です");
        }

        if (isBlank(req.getOperatorName())) {
            throw new IllegalArgumentException("操作者名は必須です");
        }

        if (req.getPeriodHours() == null) {
            throw new IllegalArgumentException("表示期間は必須です");
        }

        int period = req.getPeriodHours().intValue();
        if (period != 4 && period != 12 && period != 24) {
            throw new IllegalArgumentException("表示期間は 4, 12, 24 のみです");
        }

        if (isBlank(req.getStartDateTime())) {
            throw new IllegalArgumentException("開始日時は必須です");
        }
    }

	/**
	 * room に対する操作権を確保します。
	 *
	 * <p>
	 * ロック未取得なら取得します。
	 * すでに自分が保持中ならそのまま継続します。
	 * 他人が保持中なら例外にします。
	 * </p>
	 */
	public void ensureControlOwner(ReplayState state, String clientId, String operatorName) {
		synchronized (state) {
			expireControlIfNeededNoLock(state);

			if (!hasControllerNoLock(state)) {
				acquireControlNoLock(state, clientId, operatorName);
				return;
			}

			if (safeEquals(state.getControllerClientId(), clientId)) {
				// すでに自分が保持している場合は heartbeat だけ更新
				touchHeartbeatNoLock(state);
				return;
			}

			throw new IllegalStateException("現在の操作権は " + nullToDash(state.getControllerUserName()) + " が保持しています");
		}
	}

	/**
	 * 操作可否を返します。
	 *
	 * <p>
	 * state API / WebSocket 応答でボタン活性判定に使います。
	 * </p>
	 */
	public boolean canOperate(ReplayState state, String clientId) {
		synchronized (state) {
			expireControlIfNeededNoLock(state);

			// まだ誰も保持していない状態なら、誰でも操作可能
			if (!hasControllerNoLock(state)) {
				return true;
			}

			return safeEquals(state.getControllerClientId(), clientId);
		}
	}

	/**
	 * heartbeat を更新します。
	 *
	 * <p>
	 * 他人が送ってきた heartbeat は無視します。
	 * heartbeat 無効時は何もしません。
	 * </p>
	 */
	public void heartbeat(ReplayState state, String clientId) {
		synchronized (state) {
			expireControlIfNeededNoLock(state);

			if (!controlConfig.isHeartbeatEnabled()) {
				return;
			}

			if (hasControllerNoLock(state) && safeEquals(state.getControllerClientId(), clientId)) {
				touchHeartbeatNoLock(state);
			}
		}
	}

	/**
	 * roomId を正規化します。
	 *
	 * <p>
	 * 未指定の場合は global を返します。
	 * </p>
	 *
	 * @param roomId roomId
	 * @return 正規化済み roomId
	 */
	public String normalizeRoomId(String roomId) {
		if (roomId == null || roomId.trim().length() == 0) {
			return "global";
		}
		return roomId.trim();
	}

	/**
	 * 文字列日時を LocalDateTime に変換します。
	 *
	 * @param value 日時文字列
	 * @return LocalDateTime
	 */
	public LocalDateTime parseDateTime(String value) {
		return LocalDateTime.parse(value, FORMATTER);
	}

	/**
	 * LocalDateTime を文字列に変換します。
	 *
	 * @param value 日時
	 * @return 文字列日時
	 */
	public String formatDateTime(LocalDateTime value) {
		return value.format(FORMATTER);
	}

	/**
	 * 再生末尾時刻を計算します。
	 *
	 * <p>
	 * 開始日時 + 表示期間 - 1秒 を末尾としています。
	 * </p>
	 *
	 * @param state 対象状態
	 * @return 末尾時刻
	 */
	public LocalDateTime calcTailDateTime(ReplayState state) {
		LocalDateTime start = parseDateTime(state.getStartDateTime());
		return start.plusHours(state.getPeriodHours()).minusSeconds(1L);
	}

	/**
	 * heartbeat が有効かを返します。
	 */
	public boolean isHeartbeatEnabled() {
		return controlConfig.isHeartbeatEnabled();
	}

	/**
	 * heartbeat タイムアウト秒数を返します。
	 */
	public int getHeartbeatTimeoutSeconds() {
		return controlConfig.getHeartbeatTimeoutSeconds();
	}

	/**
	 * 新規状態の初期値を作成します。
	 *
	 * @param roomId roomId
	 * @return 初期状態
	 */
	private ReplayState createDefault(String roomId) {
		ReplayState state = new ReplayState();
		LocalDateTime today = LocalDate.now().atStartOfDay();

		state.setRoomId(roomId);
		state.setStartDateTime(formatDateTime(today));
		state.setCurrentReplayTime(formatDateTime(today));
		state.setPeriodHours(4);
		state.setPlayStatus(ReplayState.STATUS_STOPPED);
		state.setSpeed(1);
		state.setLastCommand("INIT");

		// VDU1～7 をあらかじめ作成しておく
		for (int vduNo = 1; vduNo <= 7; vduNo++) {
			state.getOrCreateVduState(vduNo);
		}

		return state;
	}

	/**
	 * ロック取得処理です。
	 */
	private void acquireControlNoLock(ReplayState state, String clientId, String operatorName) {
		long now = System.currentTimeMillis();
		state.setControllerClientId(trimToNull(clientId));
		state.setControllerUserName(trimToNull(operatorName));
		state.setLockAcquiredTime(now);
		state.setLastHeartbeatTime(now);

		// 現在操作者表示用にも反映
		state.setOperatorName(trimToNull(operatorName));
	}

	/**
	 * heartbeat 更新処理です。
	 */
	private void touchHeartbeatNoLock(ReplayState state) {
		state.setLastHeartbeatTime(System.currentTimeMillis());
	}

	/**
	 * heartbeat タイムアウトしていたらロックを解放します。
	 */
	private void expireControlIfNeededNoLock(ReplayState state) {
		if (!hasControllerNoLock(state)) {
			return;
		}

		if (!controlConfig.isHeartbeatEnabled()) {
			return;
		}

		long timeoutMillis = controlConfig.getHeartbeatTimeoutSeconds() * 1000L;
		long last = state.getLastHeartbeatTime();

		if (last <= 0L) {
			return;
		}

		long now = System.currentTimeMillis();
		if (now - last > timeoutMillis) {
			state.setControllerClientId(null);
			state.setControllerUserName(null);
			state.setLockAcquiredTime(0L);
			state.setLastHeartbeatTime(0L);

			// 現在操作者表示もクリア
			state.setOperatorName(null);
			state.setOperatorIp(null);
		}
	}

	/**
	 * 現在ロック保持者が存在するかを返します。
	 */
	private boolean hasControllerNoLock(ReplayState state) {
		return !isBlank(state.getControllerClientId());
	}

	private boolean safeEquals(String a, String b) {
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().length() == 0;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim();
		return v.length() == 0 ? null : v;
	}

	private String nullToDash(String value) {
		return value == null || value.trim().length() == 0 ? "-" : value;
	}


}