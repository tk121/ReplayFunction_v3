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

public class OperationEventRepository {

    private static final Logger log = LoggerFactory.getLogger(OperationEventRepository.class);

    private final DataSource dataSource;

    public OperationEventRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * operation_log を event 用に集計します。
     *
     * <p>
     * 戻り値は以下の意味です。
     * </p>
     * <ul>
     *   <li>null : そのバケットに operation_log 自体が存在しない</li>
     *   <li>0    : operation_log は存在するが CLICK は 0 件</li>
     *   <li>1以上: CLICK 件数</li>
     * </ul>
     */
    public Map<String, Map<LocalDateTime, Integer>> aggregateOperationEvents(
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
          + "vdu_list as ("
          + "    select 1 as vdu_no"
          + "    union all select 2"
          + "    union all select 3"
          + "    union all select 4"
          + "),"
          + "agg as ("
          + "    select"
          + "        o.vdu_no,"
          + "        date_bin("
          + "            (select bucket_interval from params),"
          + "            o.occurred_at,"
          + "            (select start_time from params)"
          + "        ) as bucket_time,"
          + "        count(*) as all_count,"
          + "        sum(case when o.action_type = 'CLICK' then 1 else 0 end) as click_count"
          + "    from operation_log o"
          + "    where o.unit_no = (select unit_no from params)"
          + "      and o.graphic_type = 'VDU'"
          + "      and o.occurred_at >= (select start_time from params)"
          + "      and o.occurred_at <  (select end_time from params)"
          + "      and o.vdu_no between 1 and 4"
          + "    group by"
          + "        o.vdu_no,"
          + "        date_bin("
          + "            (select bucket_interval from params),"
          + "            o.occurred_at,"
          + "            (select start_time from params)"
          + "        )"
          + ")"
          + "select"
          + "    v.vdu_no,"
          + "    b.bucket_time,"
          + "    a.all_count,"
          + "    a.click_count"
          + " from vdu_list v"
          + " cross join buckets b"
          + " left join agg a"
          + "   on a.vdu_no = v.vdu_no"
          + "  and a.bucket_time = b.bucket_time"
          + " order by v.vdu_no, b.bucket_time";

        Map<String, Map<LocalDateTime, Integer>> result = new LinkedHashMap<String, Map<LocalDateTime, Integer>>();
        result.put("VDU1", new LinkedHashMap<LocalDateTime, Integer>());
        result.put("VDU2", new LinkedHashMap<LocalDateTime, Integer>());
        result.put("VDU3", new LinkedHashMap<LocalDateTime, Integer>());
        result.put("VDU4", new LinkedHashMap<LocalDateTime, Integer>());

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setInt(i++, unitNo);
            ps.setTimestamp(i++, Timestamp.valueOf(startTime));
            ps.setTimestamp(i++, Timestamp.valueOf(endTime));
            ps.setString(i++, intervalExpr);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int vduNo = rs.getInt("vdu_no");

                    Timestamp ts = rs.getTimestamp("bucket_time");
                    LocalDateTime bucketTime = (ts == null) ? null : ts.toLocalDateTime();

                    Number allCountNum = (Number) rs.getObject("all_count");
                    Number clickCountNum = (Number) rs.getObject("click_count");

                    Integer allCount = (allCountNum == null) ? null : Integer.valueOf(allCountNum.intValue());
                    Integer clickCount = (clickCountNum == null) ? null : Integer.valueOf(clickCountNum.intValue());

                    Integer value;
                    if (allCount == null) {
                        value = null;
                    } else if (clickCount == null) {
                        value = 0;
                    } else {
                        value = clickCount;
                    }

                    result.get("VDU" + vduNo).put(bucketTime, value);
                }
            }

        } catch (Exception e) {
            log.error(
                    "Failed to aggregate operation event. unitNo={}, startTime={}, endTime={}, bucketMinutes={}",
                    Integer.valueOf(unitNo), startTime, endTime, Integer.valueOf(bucketMinutes), e);
            throw new RuntimeException("Failed to aggregate operation event.", e);
        }

        return result;
    }
}