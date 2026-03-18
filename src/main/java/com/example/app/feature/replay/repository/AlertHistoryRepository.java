package com.example.app.feature.replay.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.example.app.feature.replay.entity.AlertHistory;
import com.example.app.feature.replay.model.ReplayAvduAlert;

/**
 * AVDU 用アラート履歴を取得する Repository です。
 *
 * <p>
 * 現在は「発生中のアラートが1秒ごとに記録される」前提で、
 * 指定時刻以下の最新スナップショット時刻の全件を返します。
 * 将来、発生時刻＋消失時刻方式へ変わった場合は、
 * 主にこのクラスの SQL を差し替えれば対応しやすいようにしています。
 * </p>
 */
public class AlertHistoryRepository {

    private static final String TABLE_NAME = "alert_history";

    private final DataSource dataSource;

    public AlertHistoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 指定再生時刻時点で表示すべき AVDU アラート一覧を返します。
     */
    public List<ReplayAvduAlert> findAlertsAt(LocalDateTime replayTime) throws Exception {
        String sql =
                "SELECT alert_id, unit_no, occured_at, alert_tag, alert_name_1, alert_name_2, " +
                "       alert_severity, column_no, firsthit, flick, yokoku_color " +
                "  FROM " + TABLE_NAME + " " +
                " WHERE occured_at = ( " +
                "       SELECT MAX(occured_at) " +
                "         FROM " + TABLE_NAME + " " +
                "        WHERE occured_at <= ? " +
                " ) " +
                " ORDER BY unit_no ASC, column_no ASC, alert_id ASC";

        List<ReplayAvduAlert> list = new ArrayList<ReplayAvduAlert>();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(replayTime));
            rs = ps.executeQuery();

            while (rs.next()) {
                AlertHistory row = mapRow(rs);
                list.add(toReplayAlert(row));
            }

            return list;

        } finally {
            close(rs);
            close(ps);
            close(con);
        }
    }

    private AlertHistory mapRow(ResultSet rs) throws Exception {
        AlertHistory row = new AlertHistory();

        row.setAlertId(rs.getString("alert_id"));
        row.setUnitNo((Integer) rs.getObject("unit_no"));

        Timestamp ts = rs.getTimestamp("occured_at");
        if (ts != null) {
            row.setOccuredAt(ts.toLocalDateTime());
        }

        row.setAlertTag(rs.getString("alert_tag"));
        row.setAlertName1(rs.getString("alert_name_1"));
        row.setAlertName2(rs.getString("alert_name_2"));
        row.setAlertSeverity(rs.getString("alert_severity"));
        row.setColumnNo((Integer) rs.getObject("column_no"));
        row.setFirsthit((Integer) rs.getObject("firsthit"));
        row.setFlick((Integer) rs.getObject("flick"));
        row.setYokokuColor(rs.getString("yokoku_color"));

        return row;
    }

    private ReplayAvduAlert toReplayAlert(AlertHistory row) {
        ReplayAvduAlert dto = new ReplayAvduAlert();
        dto.setAlertId(row.getAlertId());
        dto.setUnitNo(row.getUnitNo());
        dto.setAlertTag(row.getAlertTag());
        dto.setAlertName1(row.getAlertName1());
        dto.setAlertName2(row.getAlertName2());
        dto.setAlertSeverity(row.getAlertSeverity());
        dto.setColumnNo(row.getColumnNo());
        dto.setFirsthit(row.getFirsthit());
        dto.setFlick(row.getFlick());
        dto.setYokokuColor(row.getYokokuColor());
        return dto;
    }

    private void close(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                // 必要ならログ出力
            }
        }
    }
}