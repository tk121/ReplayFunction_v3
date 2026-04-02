package com.example.app.feature.replay.graphic.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.feature.replay.graphic.entity.AlertLog;

/**
 * alert_log テーブルにアクセスする Repository です。
 *
 * <p>
 * replay 機能で必要となる AVDU アラート履歴の取得を担当します。
 * SQL実行と ResultSet → AlertLog への変換責務を持ちます。
 * </p>
 */
public class AlertLogRepository {

    private static final Logger log = LoggerFactory.getLogger(AlertLogRepository.class);

    private final DataSource dataSource;

    public AlertLogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 指定再生時刻時点で有効なアラート一覧を取得します。
     *
     * <p>
     * alert_tag ごとに、指定時刻以下の最新レコードを取得し、
     * そのうち action_type が RESOLVE ではないものだけを返します。
     * </p>
     *
     * @param replayTime 再生時刻
     * @return AlertLog 一覧
     * @throws Exception DBアクセス失敗時
     */
    public List<AlertLog> findActiveAlertsAt(LocalDateTime replayTime) throws Exception {

        String sql =
                "SELECT " +
                "    a.alert_id, " +
                "    a.unit_no, " +
                "    a.occurred_at, " +
                "    a.action_type, " +
                "    a.alert_tag, " +
                "    a.alert_name_1, " +
                "    a.alert_name_2, " +
                "    a.alert_severity, " +
                "    a.column_no, " +
                "    a.firsthit, " +
                "    a.flick, " +
                "    a.yokoku_color " +
                "FROM alert_log a " +
                "JOIN ( " +
                "    SELECT " +
                "        alert_tag, " +
                "        MAX(occurred_at) AS max_occurred_at " +
                "    FROM alert_log " +
                "    WHERE occurred_at <= ? " +
                "    GROUP BY alert_tag " +
                ") latest " +
                "  ON a.alert_tag = latest.alert_tag " +
                " AND a.occurred_at = latest.max_occurred_at " +
                "WHERE a.action_type <> 'RESOLVE' " +
                "ORDER BY a.column_no ASC, a.alert_tag ASC";

        List<AlertLog> list = new ArrayList<AlertLog>();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(replayTime));
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
     * ResultSet の現在行を AlertLog に変換します。
     *
     * @param rs ResultSet
     * @return AlertLog
     * @throws Exception 変換失敗時
     */
    private AlertLog mapRow(ResultSet rs) throws Exception {
        AlertLog row = new AlertLog();

        row.setAlertId(rs.getString("alert_id"));
        row.setUnitNo((Integer) rs.getObject("unit_no"));

        Timestamp ts = rs.getTimestamp("occurred_at");
        if (ts != null) {
            row.setOccurredAt(ts.toLocalDateTime());
        }

        row.setActionType(rs.getString("action_type"));
        row.setAlertTag(rs.getString("alert_tag"));
        row.setAlertName1(rs.getString("alert_name_1"));
        row.setAlertName2(rs.getString("alert_name_2"));
        row.setAlertSeverity(rs.getString("alert_severity"));
        row.setColumnNo((Integer) rs.getObject("column_no"));

        // DBカラムが NOT NULL なら getBoolean でもよい
        row.setFirsthit(rs.getBoolean("firsthit"));
        row.setFlick(rs.getBoolean("flick"));

        row.setYokokuColor(rs.getString("yokoku_color"));

        return row;
    }

    /**
     * AutoCloseable を安全に close します。
     *
     * @param closeable close対象
     */
    private void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("close failed", e);
            }
        }
    }
}