package com.example.app.feature.replay.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.example.app.feature.replay.entity.EventLog;

/**
 * event_log テーブルにアクセスする Repository です。
 *
 * <p>
 * replay 機能で必要となる操作履歴の取得を担当します。
 * SQL実行と ResultSet → EventLog への変換責務を持ちます。
 * </p>
 */
public class EventLogRepository {

    /** DB接続元 */
    private final DataSource dataSource;

    public EventLogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 指定した時間範囲に含まれるイベントを取得します。
     *
     * <p>
     * 再生エンジンの1tick分で、
     * 「前回時刻より後、今回時刻以下」のイベントを時系列順に取得するために使用します。
     * </p>
     *
     * @param fromExclusive 開始時刻（この時刻は含まない）
     * @param toInclusive 終了時刻（この時刻は含む）
     * @return 取得したイベント一覧
     * @throws Exception DBアクセス失敗時
     */
    public List<EventLog> findEventsBetween(LocalDateTime fromExclusive, LocalDateTime toInclusive) throws Exception {
        String sql =
                "SELECT event_id, unit_no, graphic_type, vdu_no, occurred_at, action_type, page_id, control_id, symbol_id, value " +
                "  FROM event_log " +
                " WHERE occurred_at > ? " +
                "   AND occurred_at <= ? " +
                " ORDER BY occurred_at ASC, event_id ASC";

        List<EventLog> list = new ArrayList<EventLog>();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement(sql);

            // SQLの検索条件に時間範囲を設定
            ps.setTimestamp(1, Timestamp.valueOf(fromExclusive));
            ps.setTimestamp(2, Timestamp.valueOf(toInclusive));

            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            return list;

        } finally {
            close(rs);
            close(ps);
            close(con);
        }
    }

    /**
     * 指定時刻時点で、各VDUの最新 OPEN イベントの page_id を取得します。
     *
     * <p>
     * GO_HEAD / GO_TAIL / APPLY_CONDITION 時に、
     * 少なくとも「どのページを表示すべきか」を復元するために使います。
     * </p>
     *
     * @param replayTime 再生時刻
     * @return key=VDU番号, value=pageId
     * @throws Exception DBアクセス失敗時
     */
    public Map<Integer, String> findLatestOpenPageMap(LocalDateTime replayTime) throws Exception {
        String sql =
                "SELECT vdu_no, page_id, occurred_at, event_id " +
                "  FROM event_log " +
                " WHERE action_type = 'OPEN' " +
                "   AND occurred_at <= ? " +
                " ORDER BY vdu_no ASC, occurred_at DESC, event_id DESC";

        Map<Integer, String> result = new LinkedHashMap<Integer, String>();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(replayTime));
            rs = ps.executeQuery();

            while (rs.next()) {
                Integer vduNo = Integer.valueOf(rs.getInt("vdu_no"));

                // 各VDUについて最初に出てきた行が最新なので、その1件だけ採用する
                if (!result.containsKey(vduNo)) {
                    result.put(vduNo, rs.getString("page_id"));
                }
            }
            return result;

        } finally {
            close(rs);
            close(ps);
            close(con);
        }
    }

    /**
     * ResultSet の現在行を EventLog に変換します。
     *
     * @param rs ResultSet
     * @return EventLog
     * @throws Exception 変換失敗時
     */
    private EventLog mapRow(ResultSet rs) throws Exception {
        EventLog eventLog = new EventLog();
        eventLog.setEventId(rs.getLong("event_id"));
        eventLog.setUnitNo((Integer) rs.getObject("unit_no"));
        eventLog.setGraphicType(rs.getString("graphic_type"));
        eventLog.setVduNo(rs.getInt("vdu_no"));

        Timestamp ts = rs.getTimestamp("occurred_at");
        if (ts != null) {
            eventLog.setOccurredAt(ts.toLocalDateTime());
        }

        eventLog.setActionType(rs.getString("event_type"));
        eventLog.setPageId(rs.getString("page_id"));
        eventLog.setControlId(rs.getString("control_id"));
        eventLog.setSymbolId(rs.getString("symbol_id"));
        eventLog.setValue(rs.getString("value"));
        return eventLog;
    }

    /**
     * AutoCloseable を安全に close します。
     *
     * <p>
     * close時の例外は握りつぶし、後続処理へ影響を出さないようにしています。
     * </p>
     *
     * @param closeable close対象
     */
    private void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 必要ならログ出力
            }
        }
    }
}