package com.example.app.feature.replay.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.app.feature.replay.common.model.ReplayMode;
import com.example.app.feature.replay.common.model.ReplayState;
import com.example.app.feature.replay.common.service.ReplaySessionService;
import com.example.app.feature.replay.event.dto.ReplayEventResponse;
import com.example.app.feature.replay.event.model.EventCountPoint;
import com.example.app.feature.replay.event.repository.AlertCountPerMinuteRepository;
import com.example.app.feature.replay.event.repository.VduOperationCountPerMinuteRepository;

public class ReplayEventService {

    private static final DateTimeFormatter KEY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    private final ReplaySessionService sessionService;
    private final VduOperationCountPerMinuteRepository vduRepository;
    private final AlertCountPerMinuteRepository alertRepository;

    public ReplayEventService(ReplaySessionService sessionService,
            VduOperationCountPerMinuteRepository vduRepository,
            AlertCountPerMinuteRepository alertRepository) {
        this.sessionService = sessionService;
        this.vduRepository = vduRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * 現在共有されている event 系列を返します。
     *
     * <p>
     * 非操作者が index.html を開いたときの初期表示で使用します。
     * まだ条件反映されていない場合は空系列を返します。
     * </p>
     */
    public ReplayEventResponse getCurrentSharedSeries(String roomId) {
        ReplayState state = sessionService.getState(roomId);

        ReplayEventResponse response = new ReplayEventResponse();
        response.setSeries(copySeries(state.getEventSeries()));
        return response;
    }

    /**
     * 現在の ReplayState の条件をもとに、その日分の event 集計済みデータを取得して state に保持します。
     *
     * <p>
     * 操作者の APPLY_CONDITION 時に使用します。
     * </p>
     */
    public Map<String, Map<String, Integer>> loadAndStoreForState(ReplayState state) {
        LocalDate targetDate = resolveTargetDate(state);

        List<EventCountPoint> vduPoints =
                vduRepository.findByDay(state.getUnitNo().intValue(), targetDate);

        List<EventCountPoint> alertPoints =
                alertRepository.findByDay(state.getUnitNo().intValue(), targetDate);

        Map<String, Map<String, Integer>> series = createEmptySeries();
        LocalDateTime newest = null;

        for (EventCountPoint point : vduPoints) {
            String key = toVduKey(point.getSystemNo());
            if (!series.containsKey(key)) {
                continue;
            }

            series.get(key).put(
                    point.getBucketStart().format(KEY_FORMAT),
                    Integer.valueOf(point.getCount()));

            if (newest == null || point.getBucketStart().isAfter(newest)) {
                newest = point.getBucketStart();
            }
        }

        for (EventCountPoint point : alertPoints) {
            String key = toAlertKey(point.getSystemNo());
            if (!series.containsKey(key)) {
                continue;
            }

            series.get(key).put(
                    point.getBucketStart().format(KEY_FORMAT),
                    Integer.valueOf(point.getCount()));

            if (newest == null || point.getBucketStart().isAfter(newest)) {
                newest = point.getBucketStart();
            }
        }

        state.setEventSeries(series);
        state.setEventLastLoadedBucketStart(newest);
        state.setConditionApplied(true);

        return copySeries(series);
    }

    /**
     * REALTIME 用に、最後に取り込んだ bucket_start より後の差分だけ取得して state にマージします。
     *
     * <p>
     * ReplayEngine の tick から呼ばれる想定です。
     * </p>
     */
    public Map<String, Map<String, Integer>> loadRealtimeAppendAndMerge(ReplayState state) {
        if (state.getEventLastLoadedBucketStart() == null) {
            return createEmptySeries();
        }

        List<EventCountPoint> vduPoints =
                vduRepository.findAfter(state.getUnitNo().intValue(), state.getEventLastLoadedBucketStart());

        List<EventCountPoint> alertPoints =
                alertRepository.findAfter(state.getUnitNo().intValue(), state.getEventLastLoadedBucketStart());

        Map<String, Map<String, Integer>> append = createEmptySeries();
        LocalDateTime newest = state.getEventLastLoadedBucketStart();

        for (EventCountPoint point : vduPoints) {
            String key = toVduKey(point.getSystemNo());
            String timeKey = point.getBucketStart().format(KEY_FORMAT);

            append.get(key).put(timeKey, Integer.valueOf(point.getCount()));
            ensureSeriesBucket(state, key).put(timeKey, Integer.valueOf(point.getCount()));

            if (point.getBucketStart().isAfter(newest)) {
                newest = point.getBucketStart();
            }
        }

        for (EventCountPoint point : alertPoints) {
            String key = toAlertKey(point.getSystemNo());
            String timeKey = point.getBucketStart().format(KEY_FORMAT);

            append.get(key).put(timeKey, Integer.valueOf(point.getCount()));
            ensureSeriesBucket(state, key).put(timeKey, Integer.valueOf(point.getCount()));

            if (point.getBucketStart().isAfter(newest)) {
                newest = point.getBucketStart();
            }
        }

        state.setEventLastLoadedBucketStart(newest);
        return append;
    }

    private LocalDate resolveTargetDate(ReplayState state) {
        if (state.getReplayMode() == ReplayMode.REALTIME) {
            return state.getCurrentReplayTime().toLocalDate();
        }
        return state.getStartDateTime().toLocalDate();
    }

    private String toVduKey(int vduNo) {
        if (vduNo == 1) return "vdu1";
        if (vduNo == 2) return "vdu2";
        if (vduNo == 3) return "vdu3";
        if (vduNo == 4) return "vdu4";
        return "vdu" + vduNo;
    }

    private String toAlertKey(int systemNo) {
        if (systemNo == 1) return "alert1";
        if (systemNo == 2) return "alert2";
        if (systemNo == 3) return "alertElectrical";
        return "alert" + systemNo;
    }

    private Map<String, Map<String, Integer>> createEmptySeries() {
        Map<String, Map<String, Integer>> series = new LinkedHashMap<String, Map<String, Integer>>();
        series.put("vdu1", new LinkedHashMap<String, Integer>());
        series.put("vdu2", new LinkedHashMap<String, Integer>());
        series.put("vdu3", new LinkedHashMap<String, Integer>());
        series.put("vdu4", new LinkedHashMap<String, Integer>());
        series.put("alert1", new LinkedHashMap<String, Integer>());
        series.put("alert2", new LinkedHashMap<String, Integer>());
        series.put("alertElectrical", new LinkedHashMap<String, Integer>());
        return series;
    }

    private Map<String, Integer> ensureSeriesBucket(ReplayState state, String key) {
        if (state.getEventSeries() == null) {
            state.setEventSeries(createEmptySeries());
        }

        Map<String, Integer> bucket = state.getEventSeries().get(key);
        if (bucket == null) {
            bucket = new LinkedHashMap<String, Integer>();
            state.getEventSeries().put(key, bucket);
        }
        return bucket;
    }

    private Map<String, Map<String, Integer>> copySeries(Map<String, Map<String, Integer>> source) {
        Map<String, Map<String, Integer>> copy = createEmptySeries();

        if (source == null) {
            return copy;
        }

        for (Map.Entry<String, Map<String, Integer>> entry : source.entrySet()) {
            if (!copy.containsKey(entry.getKey())) {
                copy.put(entry.getKey(), new LinkedHashMap<String, Integer>());
            }
            if (entry.getValue() != null) {
                copy.get(entry.getKey()).putAll(entry.getValue());
            }
        }

        return copy;
    }
}