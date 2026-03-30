package com.example.app.feature.replay.event.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertEventRepository {

    private static final Logger log = LoggerFactory.getLogger(AlertEventRepository.class);

    private final DataSource dataSource;

    public AlertEventRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * alert_log を event 用に集計します。
     *
     * <p>
     * 戻り値は以下の意味です。
     * </p>
     * <ul>
     *   <li>null : そのバケットに alert_log が存在しない</li>
     *   <li>1以上: alert_log 件数</li>
     * </ul>
     */
    public Map<LocalDateTime, Integer> aggregateAlertEvents(
            int unitNo,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int bucketMinutes) {

        String intervalExpr = bucketMinutes + " minute";

        String sql =
            "with params as ("
          + "    select"
          + "        ?::integer as unit_no,"
          + "        ?::timestamp as start_time,"
          + "        ?::timestamp as end_time,"
          + "        ?::interval as bucket_interval"
          + "),"
          + "buckets as ("
          + "    select generate_series("
          + "        (select start_time from params),"
          + "        (select end_time from params) - (select bucket_interval from params),"
          + "        (select bucket_interval from params)"
          + "    ) as bucket_time"
          + "),"
          + "agg as ("
          + "    select"
          + "        date_bin("
          + "            (select bucket_interval from params),"
          + "            a.occurred_at,"
          + "            (select start_time from params)"
          + "        ) as bucket_time,"
          + "        count(*) as alert_count"
          + "    from alert_log a"
          + "    where a.unit_no = (select unit_no from params)"
          + "      and a.occurred_at >= (select start_time from params)"
          + "      and a.occurred_at <  (select end_time from params)"
          + "    group by"
          + "        date_bin("
          + "            (select bucket_interval from params),"
          + "            a.occurred_at,"
          + "            (select start_time from params)"
          + "        )"
          + ")"
          + "select"
          + "    b.bucket_time,"
          + "    a.alert_count"
          + " from buckets b"
          + " left join agg a"
          + "   on a.bucket_time = b.bucket_time"
          + " order by b.bucket_time";

        Map<LocalDateTime, Integer> result = new LinkedHashMap<LocalDateTime, Integer>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setInt(i++, unitNo);
            ps.setTimestamp(i++, Timestamp.valueOf(startTime));
            ps.setTimestamp(i++, Timestamp.valueOf(endTime));
            ps.setString(i++, intervalExpr);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("bucket_time");
                    LocalDateTime bucketTime = (ts == null) ? null : ts.toLocalDateTime();

                    Number alertCountNum = (Number) rs.getObject("alert_count");
                    Integer alertCount = (alertCountNum == null) ? null : Integer.valueOf(alertCountNum.intValue());

                    result.put(bucketTime, alertCount);
                }
            }

        } catch (Exception e) {
            log.error(
                    "Failed to aggregate alert event. unitNo={}, startTime={}, endTime={}, bucketMinutes={}",
                    Integer.valueOf(unitNo), startTime, endTime, Integer.valueOf(bucketMinutes), e);
            throw new RuntimeException("Failed to aggregate alert event.", e);
        }

        return result;
    }
}