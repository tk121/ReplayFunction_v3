package com.example.app.feature.trend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.example.app.feature.trend.model.TrendDefinition;

public class TrendDefinitionRepository {

    private final DataSource dataSource;

    public TrendDefinitionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<TrendDefinition> findByUserId(String userId) {
        if (userId == null || userId.trim().length() == 0) {
            return new ArrayList<TrendDefinition>();
        }

        String sql =
                "select " +
                "    d.trend_id, " +
                "    d.user_id, " +
                "    d.trend_name, " +
                "    dd.device_id " +
                "from trend_definition d " +
                "left join trend_definition_device dd " +
                "  on dd.trend_id = d.trend_id " +
                "where d.user_id = ? " +
                "order by d.trend_id asc, dd.sort_order asc, dd.device_id asc";

        Map<Long, TrendDefinition> map = new LinkedHashMap<Long, TrendDefinition>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(userId));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long trendId = Long.valueOf(rs.getLong("trend_id"));

                    TrendDefinition definition = map.get(trendId);
                    if (definition == null) {
                        definition = new TrendDefinition();
                        definition.setTrendId(trendId);
                        definition.setUserId(String.valueOf(rs.getLong("user_id")));
                        definition.setTrendName(rs.getString("trend_name"));
                        map.put(trendId, definition);
                    }

                    String deviceId = rs.getString("device_id");
                    if (deviceId != null && deviceId.trim().length() > 0) {
                        definition.getDeviceIds().add(deviceId);
                    }
                }
            }

            return new ArrayList<TrendDefinition>(map.values());

        } catch (Exception e) {
            throw new RuntimeException("trend_definition の取得に失敗しました", e);
        }
    }

    public TrendDefinition findByTrendId(Long trendId) {
        if (trendId == null) {
            return null;
        }

        String sql =
                "select " +
                "    d.trend_id, " +
                "    d.user_id, " +
                "    d.trend_name, " +
                "    dd.device_id " +
                "from trend_definition d " +
                "left join trend_definition_device dd " +
                "  on dd.trend_id = d.trend_id " +
                "where d.trend_id = ? " +
                "order by dd.sort_order asc, dd.device_id asc";

        TrendDefinition definition = null;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, trendId.longValue());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (definition == null) {
                        definition = new TrendDefinition();
                        definition.setTrendId(Long.valueOf(rs.getLong("trend_id")));
                        definition.setUserId(String.valueOf(rs.getLong("user_id")));
                        definition.setTrendName(rs.getString("trend_name"));
                    }

                    String deviceId = rs.getString("device_id");
                    if (deviceId != null && deviceId.trim().length() > 0) {
                        definition.getDeviceIds().add(deviceId);
                    }
                }
            }

            return definition;

        } catch (Exception e) {
            throw new RuntimeException("trend_definition の取得に失敗しました", e);
        }
    }

    public TrendDefinition insert(String userId, String trendName, List<String> deviceIds) {
        String insertDefinitionSql =
                "insert into trend_definition (user_id, trend_name) values (?, ?)";
        String currentValueSql = "select currval(pg_get_serial_sequence('trend_definition', 'trend_id'))";
        String insertDeviceSql =
                "insert into trend_definition_device (trend_id, device_id, sort_order) values (?, ?, ?)";

        Connection con = null;
        PreparedStatement psDefinition = null;
        PreparedStatement psCurrval = null;
        PreparedStatement psDevice = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);

            psDefinition = con.prepareStatement(insertDefinitionSql);
            psDefinition.setLong(1, Long.parseLong(userId));
            psDefinition.setString(2, trendName);
            psDefinition.executeUpdate();

            psCurrval = con.prepareStatement(currentValueSql);
            rs = psCurrval.executeQuery();
            if (!rs.next()) {
                throw new IllegalStateException("trend_id の採番取得に失敗しました");
            }

            Long trendId = Long.valueOf(rs.getLong(1));

            psDevice = con.prepareStatement(insertDeviceSql);
            for (int i = 0; i < deviceIds.size(); i++) {
                psDevice.setLong(1, trendId.longValue());
                psDevice.setString(2, deviceIds.get(i));
                psDevice.setInt(3, i + 1);
                psDevice.addBatch();
            }
            psDevice.executeBatch();

            con.commit();
            return findByTrendId(trendId);

        } catch (Exception e) {
            rollbackQuietly(con);
            throw new RuntimeException("trend_definition の登録に失敗しました", e);
        } finally {
            closeQuietly(rs);
            closeQuietly(psDevice);
            closeQuietly(psCurrval);
            closeQuietly(psDefinition);
            resetAutoCommitQuietly(con);
            closeQuietly(con);
        }
    }

    public TrendDefinition update(Long trendId, String userId, String trendName, List<String> deviceIds) {
        String updateDefinitionSql =
                "update trend_definition set trend_name = ? where trend_id = ? and user_id = ?";
        String deleteDeviceSql =
                "delete from trend_definition_device where trend_id = ?";
        String insertDeviceSql =
                "insert into trend_definition_device (trend_id, device_id, sort_order) values (?, ?, ?)";

        Connection con = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psDeleteDevice = null;
        PreparedStatement psInsertDevice = null;

        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);

            psUpdate = con.prepareStatement(updateDefinitionSql);
            psUpdate.setString(1, trendName);
            psUpdate.setLong(2, trendId.longValue());
            psUpdate.setLong(3, Long.parseLong(userId));

            int updated = psUpdate.executeUpdate();
            if (updated == 0) {
                throw new IllegalStateException("更新対象の trend が存在しません");
            }

            psDeleteDevice = con.prepareStatement(deleteDeviceSql);
            psDeleteDevice.setLong(1, trendId.longValue());
            psDeleteDevice.executeUpdate();

            psInsertDevice = con.prepareStatement(insertDeviceSql);
            for (int i = 0; i < deviceIds.size(); i++) {
                psInsertDevice.setLong(1, trendId.longValue());
                psInsertDevice.setString(2, deviceIds.get(i));
                psInsertDevice.setInt(3, i + 1);
                psInsertDevice.addBatch();
            }
            psInsertDevice.executeBatch();

            con.commit();
            return findByTrendId(trendId);

        } catch (Exception e) {
            rollbackQuietly(con);
            throw new RuntimeException("trend_definition の更新に失敗しました", e);
        } finally {
            closeQuietly(psInsertDevice);
            closeQuietly(psDeleteDevice);
            closeQuietly(psUpdate);
            resetAutoCommitQuietly(con);
            closeQuietly(con);
        }
    }

    public void delete(Long trendId, String userId) {
        String deleteDeviceSql =
                "delete from trend_definition_device where trend_id = ?";
        String deleteDefinitionSql =
                "delete from trend_definition where trend_id = ? and user_id = ?";

        Connection con = null;
        PreparedStatement psDeleteDevice = null;
        PreparedStatement psDeleteDefinition = null;

        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);

            psDeleteDevice = con.prepareStatement(deleteDeviceSql);
            psDeleteDevice.setLong(1, trendId.longValue());
            psDeleteDevice.executeUpdate();

            psDeleteDefinition = con.prepareStatement(deleteDefinitionSql);
            psDeleteDefinition.setLong(1, trendId.longValue());
            psDeleteDefinition.setLong(2, Long.parseLong(userId));

            int deleted = psDeleteDefinition.executeUpdate();
            if (deleted == 0) {
                throw new IllegalStateException("削除対象の trend が存在しません");
            }

            con.commit();

        } catch (Exception e) {
            rollbackQuietly(con);
            throw new RuntimeException("trend_definition の削除に失敗しました", e);
        } finally {
            closeQuietly(psDeleteDefinition);
            closeQuietly(psDeleteDevice);
            resetAutoCommitQuietly(con);
            closeQuietly(con);
        }
    }

    private void rollbackQuietly(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (Exception ignore) {
            }
        }
    }

    private void resetAutoCommitQuietly(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
            } catch (Exception ignore) {
            }
        }
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignore) {
            }
        }
    }
}