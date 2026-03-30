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

import com.example.app.feature.replay.graphic.entity.PlantDataLog;

public class PlantDataLogRepository {

	private static final Logger log = LoggerFactory.getLogger(PlantDataLogRepository.class);

	private final DataSource dataSource;

	public PlantDataLogRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 指定 unit_no・時間範囲の plant_data_log を取得します。
	 *
	 * <p>
	 * fromExclusive < occurred_at <= toInclusive
	 * </p>
	 */
	public List<PlantDataLog> findByUnitNoAndOccurredAtRange(
			Integer unitNo,
			LocalDateTime fromExclusive,
			LocalDateTime toInclusive) {

		List<PlantDataLog> result = new ArrayList<PlantDataLog>();

		if (unitNo == null || fromExclusive == null || toInclusive == null) {
			return result;
		}

		String sql = "select "
				+ "    data_id"
				+ "  , unit_no"
				+ "  , occurred_at"
				+ "  , symbol"
				+ "  , value_locator"
				+ "  , value_type"
				+ "  , ai_value"
				+ "  , di_value"
				+ "  , status"
				+ " from plant_data_log"
				+ " where unit_no = ?"
				+ "   and occurred_at > ?"
				+ "   and occurred_at <= ?"
				+ " order by occurred_at asc, data_id asc";

		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, unitNo.intValue());
			ps.setTimestamp(2, Timestamp.valueOf(fromExclusive));
			ps.setTimestamp(3, Timestamp.valueOf(toInclusive));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					PlantDataLog row = new PlantDataLog();
					row.setDataId(Long.valueOf(rs.getLong("data_id")));
					row.setUnitNo(Integer.valueOf(rs.getInt("unit_no")));
					row.setOccurredAt(rs.getTimestamp("occurred_at").toLocalDateTime());
					row.setSymbol(rs.getString("symbol"));
					row.setValueLocator(rs.getString("value_locator"));
					row.setValueType(rs.getString("value_type"));
					row.setAiValue(rs.getBigDecimal("ai_value"));

					int diValue = rs.getInt("di_value");
					if (rs.wasNull()) {
						row.setDiValue(null);
					} else {
						row.setDiValue(Integer.valueOf(diValue));
					}

					row.setStatus(rs.getString("status"));
					result.add(row);
				}
			}

		} catch (Exception e) {
			log.error("Failed to fetch plant_data_log. unitNo={}, fromExclusive={}, toInclusive={}",
					unitNo, fromExclusive, toInclusive, e);
			throw new RuntimeException("plant_data_log の取得に失敗しました", e);
		}

		return result;
	}
}
