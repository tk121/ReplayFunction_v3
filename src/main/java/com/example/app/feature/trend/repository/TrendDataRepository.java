package com.example.app.feature.trend.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.example.app.feature.trend.model.TrendDataPoint;

public class TrendDataRepository {

    private final DataSource dataSource;

    public TrendDataRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 指定 device(symbol) 群の、指定日1日分の trend データを取得します。
     *
     * <p>
     * plant_data_log.symbol を trend の deviceId として扱います。
     * 値は ai_value 優先、ai_value が null のとき di_value を数値化して使います。
     * </p>
     */
    public Map<String, List<TrendDataPoint>> findHistorySeriesByDevices(
            List<String> deviceIds,
            LocalDate targetDate) {

        Map<String, List<TrendDataPoint>> result =
                new LinkedHashMap<String, List<TrendDataPoint>>();

        if (deviceIds == null || deviceIds.isEmpty()) {
            return result;
        }

        for (String deviceId : deviceIds) {
            result.put(deviceId, new ArrayList<TrendDataPoint>());
        }

        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();

        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append("    symbol, ");
        sql.append("    occurred_at, ");
        sql.append("    ai_value, ");
        sql.append("    di_value ");
        sql.append("from plant_data_log ");
        sql.append("where occurred_at >= ? ");
        sql.append("  and occurred_at < ? ");
        sql.append("  and symbol in (");
        appendPlaceholders(sql, deviceIds.size());
        sql.append(") ");
        sql.append("order by symbol asc, occurred_at asc, data_id asc");

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            ps.setTimestamp(index++, Timestamp.valueOf(from));
            ps.setTimestamp(index++, Timestamp.valueOf(to));

            for (String deviceId : deviceIds) {
                ps.setString(index++, deviceId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String deviceId = rs.getString("symbol");
                    Timestamp ts = rs.getTimestamp("occurred_at");

                    BigDecimal value = resolveValue(rs);

                    TrendDataPoint point = new TrendDataPoint(
                            deviceId,
                            ts != null ? ts.toLocalDateTime() : null,
                            value);

                    List<TrendDataPoint> series = result.get(deviceId);
                    if (series == null) {
                        series = new ArrayList<TrendDataPoint>();
                        result.put(deviceId, series);
                    }
                    series.add(point);
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("plant_data_log の履歴取得に失敗しました", e);
        }
    }

    /**
     * 指定 device(symbol) 群の差分データを取得します。
     *
     * <p>
     * fromExclusive < occurred_at <= toInclusive
     * </p>
     */
    public List<TrendDataPoint> findRealtimeDeltaByDevices(
            List<String> deviceIds,
            LocalDateTime fromExclusive,
            LocalDateTime toInclusive) {

        List<TrendDataPoint> result = new ArrayList<TrendDataPoint>();

        if (deviceIds == null || deviceIds.isEmpty()) {
            return result;
        }
        if (fromExclusive == null || toInclusive == null) {
            return result;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append("    symbol, ");
        sql.append("    occurred_at, ");
        sql.append("    ai_value, ");
        sql.append("    di_value ");
        sql.append("from plant_data_log ");
        sql.append("where occurred_at > ? ");
        sql.append("  and occurred_at <= ? ");
        sql.append("  and symbol in (");
        appendPlaceholders(sql, deviceIds.size());
        sql.append(") ");
        sql.append("order by symbol asc, occurred_at asc, data_id asc");

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            ps.setTimestamp(index++, Timestamp.valueOf(fromExclusive));
            ps.setTimestamp(index++, Timestamp.valueOf(toInclusive));

            for (String deviceId : deviceIds) {
                ps.setString(index++, deviceId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new TrendDataPoint(
                            rs.getString("symbol"),
                            rs.getTimestamp("occurred_at").toLocalDateTime(),
                            resolveValue(rs)));
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("plant_data_log の差分取得に失敗しました", e);
        }
    }

    private BigDecimal resolveValue(ResultSet rs) throws Exception {
        BigDecimal aiValue = rs.getBigDecimal("ai_value");
        if (aiValue != null) {
            return aiValue;
        }

        Integer diValue = (Integer) rs.getObject("di_value");
        if (diValue != null) {
            return BigDecimal.valueOf(diValue.longValue());
        }

        return null;
    }

    private void appendPlaceholders(StringBuilder sql, int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
    }
}