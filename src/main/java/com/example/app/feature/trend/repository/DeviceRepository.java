package com.example.app.feature.trend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class DeviceRepository {

    private final DataSource dataSource;

    public DeviceRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * device 一覧を返します。
     *
     * <p>
     * 現段階では plant_data_log.symbol の distinct を device 一覧とみなします。
     * 必要なら専用 master テーブルに差し替えてください。
     * </p>
     */
    public List<String> findAllDeviceIds() {
        String sql =
                "select distinct symbol " +
                "from plant_data_log " +
                "where symbol is not null " +
                "order by symbol asc";

        List<String> result = new ArrayList<String>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String symbol = rs.getString("symbol");
                if (symbol != null && symbol.trim().length() > 0) {
                    result.add(symbol);
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("device 一覧の取得に失敗しました", e);
        }
    }
}