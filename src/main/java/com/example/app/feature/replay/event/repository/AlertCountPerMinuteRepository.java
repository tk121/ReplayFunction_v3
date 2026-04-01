package com.example.app.feature.replay.event.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.feature.replay.event.model.EventCountPoint;

public class AlertCountPerMinuteRepository {

    private static final Logger log = LoggerFactory.getLogger(AlertCountPerMinuteRepository.class);

    private final DataSource dataSource;

    public AlertCountPerMinuteRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<EventCountPoint> findByDay(int unitNo, LocalDate targetDate) {
        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();

        String sql =
                "select system_no, bucket_start, alerts_count " +
                "  from alert_counts_per_minute " +
                " where unit_no = ? " +
                "   and bucket_start >= ? " +
                "   and bucket_start < ? " +
                " order by system_no asc, bucket_start asc";

        List<EventCountPoint> result = new ArrayList<EventCountPoint>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, unitNo);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new EventCountPoint(
                            rs.getInt("system_no"),
                            rs.getTimestamp("bucket_start").toLocalDateTime(),
                            rs.getInt("alerts_count")));
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch alert_counts_per_minute. unitNo={}, targetDate={}", unitNo, targetDate, e);
            throw new RuntimeException("alert_counts_per_minute の取得に失敗しました", e);
        }
    }

    public List<EventCountPoint> findAfter(int unitNo, LocalDateTime lastBucketStart) {
        String sql =
                "select system_no, bucket_start, alerts_count " +
                "  from alert_counts_per_minute " +
                " where unit_no = ? " +
                "   and bucket_start > ? " +
                " order by system_no asc, bucket_start asc";

        List<EventCountPoint> result = new ArrayList<EventCountPoint>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, unitNo);
            ps.setTimestamp(2, Timestamp.valueOf(lastBucketStart));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new EventCountPoint(
                            rs.getInt("system_no"),
                            rs.getTimestamp("bucket_start").toLocalDateTime(),
                            rs.getInt("alerts_count")));
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch alert_counts_per_minute after lastBucketStart. unitNo={}, lastBucketStart={}",
                    unitNo, lastBucketStart, e);
            throw new RuntimeException("alert_counts_per_minute の差分取得に失敗しました", e);
        }
    }
}