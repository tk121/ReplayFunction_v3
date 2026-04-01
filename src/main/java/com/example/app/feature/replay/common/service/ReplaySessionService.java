package com.example.app.feature.replay.common.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.replay.common.model.ReplayMode;
import com.example.app.feature.replay.common.model.ReplayState;
import com.example.app.feature.replay.graphic.dto.ReplayControlRequest;

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

    public ReplayState getState(String roomId) { return getOrCreate(roomId); }
    public Collection<ReplayState> getAllStates() { return new ArrayList<ReplayState>(stateMap.values()); }

    public void validateControlRequest(ReplayControlRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("リクエストがありません");
        }
        if (req.getDisplayHours() == null) {
            throw new IllegalArgumentException("表示期間は必須です");
        }
        int displayHours = req.getDisplayHours().intValue();
        if (displayHours != 4 && displayHours != 12 && displayHours != 24) {
            throw new IllegalArgumentException("表示期間は 4, 12, 24 のみです");
        }
        ReplayMode mode = parseReplayMode(req.getReplayMode());
        if (req.getUnitNo() == null) {
            throw new IllegalArgumentException("unitNo は必須です");
        }
        if (mode == ReplayMode.HISTORY && req.getStartDateTime() == null) {
            throw new IllegalArgumentException("過去再生では開始日時が必須です");
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
    public void transferControlAtLogin(ReplayState state, LoginUser loginUser) {
		synchronized (state) {
            if (!loginUser.isCanControl()) {
				return;
			}
            state.setControllerUserId(loginUser.getUserId());
            state.setControllerUserName(loginUser.getUserName());
            state.setOperatorName(loginUser.getUserName());
        }
    }

    public boolean canOperate(ReplayState state, LoginUser loginUser) {
        synchronized (state) {
            if (loginUser == null || !loginUser.isCanControl()) {
                return false;
            }
            if (isBlank(state.getControllerUserId())) {
                return false;
            }
            return state.getControllerUserId().equals(loginUser.getUserId());
        }
			}

    public void assertCanOperate(ReplayState state, LoginUser loginUser) {
        if (loginUser == null) {
            throw new IllegalStateException("ログインしていません");
        }
        if (!loginUser.isCanControl()) {
            throw new IllegalStateException("このユーザーは操作権を保有できません");
        }
        if (!canOperate(state, loginUser)) {
			throw new IllegalStateException("現在の操作権は " + nullToDash(state.getControllerUserName()) + " が保持しています");
		}
	}

    public void applySharedFields(ReplayState state, ReplayControlRequest req, String remoteIp) {
		synchronized (state) {
            ReplayMode mode = parseReplayMode(req.getReplayMode());
            state.setReplayMode(mode);
            state.setUnitNo(req.getUnitNo());
            state.setOperatorIp(remoteIp);
            if (mode == ReplayMode.HISTORY) {
                state.setStartDateTime(req.getStartDateTime());
                if (state.getCurrentReplayTime() == null || "APPLY_CONDITION".equals(req.getCommand())) {
                    state.setCurrentReplayTime(req.getStartDateTime());
                }
            } else {
                LocalDateTime now = LocalDateTime.now().withNano(0);
                state.setStartDateTime(now.toLocalDate().atStartOfDay());
                if (state.getCurrentReplayTime() == null || "APPLY_CONDITION".equals(req.getCommand())) {
                    state.setCurrentReplayTime(now);
			}
		}
	}
			}

    public ReplayMode parseReplayMode(String value) {
        if (value == null || value.trim().length() == 0) {
            return ReplayMode.HISTORY;
			}
        return ReplayMode.valueOf(value.trim().toUpperCase());
	}

	public String normalizeRoomId(String roomId) {
		if (roomId == null || roomId.trim().length() == 0) {
			return "replayMode";
		}
		return roomId.trim();
	}

    public LocalDateTime parseDateTime(String value) { return LocalDateTime.parse(value, FORMATTER); }
    public String formatDateTime(LocalDateTime value) { return value.format(FORMATTER); }

    /** 操作者の表示期間を基準に次の表示期間末尾へ移動 */
    public LocalDateTime calcTailDateTime(ReplayState state, int displayHours) {
        if (state.getReplayMode() == ReplayMode.REALTIME) {
            return state.getCurrentReplayTime();
        }
        LocalDateTime current = state.getCurrentReplayTime();
        LocalDateTime base = state.getStartDateTime();
        long secondsPerWindow = displayHours * 3600L;
        long elapsed = java.time.Duration.between(base, current).getSeconds();
        long nextWindowIndex = Math.floorDiv(Math.max(elapsed, 0L), secondsPerWindow) + 1L;
        return base.plusSeconds(nextWindowIndex * secondsPerWindow);
	}

	private ReplayState createDefault(String roomId) {
		ReplayState state = new ReplayState();
		LocalDateTime today = LocalDate.now().atStartOfDay();
		state.setRoomId(roomId);
        state.setUnitNo(Integer.valueOf(1));
        state.setReplayMode(ReplayMode.HISTORY);
		state.setStartDateTime(today);
		state.setCurrentReplayTime(today);
		state.setPlayStatus(ReplayState.STATUS_STOPPED);
		state.setSpeed(1);
		state.setLastCommand("INIT");
		for (int vduNo = 1; vduNo <= 7; vduNo++) {
			state.getOrCreateVduState(vduNo);
		}
		return state;
	}

    private boolean isBlank(String value) { return value == null || value.trim().length() == 0; }
    private String nullToDash(String value) { return isBlank(value) ? "-" : value; }
}
