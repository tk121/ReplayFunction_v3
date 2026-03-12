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
     * リクエストから基本項目を state に反映します。
     *
     * <p>
     * 操作者名、操作者IP、開始日時、表示期間などの
     * 再生条件を state に設定します。
     * </p>
     *
     * @param state 対象状態
     * @param req リクエスト
     * @param remoteIp 操作者IP
     */
    public void applyBaseFields(ReplayState state, ReplayControlRequest req, String remoteIp) {
        validateRequest(req);

        synchronized (state) {
            state.setOperatorName(req.getOperatorName().trim());
            state.setOperatorIp(remoteIp);
            state.setStartDateTime(req.getStartDateTime());
            state.setPeriodHours(req.getPeriodHours().intValue());
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
     * リクエスト内容を検証します。
     *
     * <p>
     * 必須項目の未設定や、許可されない表示期間などをチェックします。
     * </p>
     *
     * @param req リクエスト
     */
    private void validateRequest(ReplayControlRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("リクエストがありません");
        }

        if (req.getOperatorName() == null || req.getOperatorName().trim().length() == 0) {
            throw new IllegalArgumentException("操作者名は必須です");
        }

        if (req.getPeriodHours() == null) {
            throw new IllegalArgumentException("表示期間は必須です");
        }

        int period = req.getPeriodHours().intValue();
        if (period != 4 && period != 12 && period != 24) {
            throw new IllegalArgumentException("表示期間は 4, 12, 24 のみです");
        }

        if (req.getStartDateTime() == null || req.getStartDateTime().trim().length() == 0) {
            throw new IllegalArgumentException("開始日時は必須です");
        }
    }
}