package com.example.app.feature.replay.event.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.app.feature.replay.common.model.ReplayMode;
import com.example.app.feature.replay.common.model.ReplayState;
import com.example.app.feature.replay.event.dto.ReplayEventResponse;
import com.example.app.feature.replay.event.repository.AlertEventRepository;
import com.example.app.feature.replay.event.repository.OperationEventRepository;
import com.example.app.feature.replay.graphic.service.ReplaySessionService;

public class ReplayEventService {
    private static final DateTimeFormatter KEY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    private final ReplaySessionService sessionService;
    private final OperationEventRepository operationEventRepository;
    private final AlertEventRepository alertEventRepository;

    public ReplayEventService(ReplaySessionService sessionService,
            OperationEventRepository operationEventRepository,
            AlertEventRepository alertEventRepository) {
        this.sessionService = sessionService;
        this.operationEventRepository = operationEventRepository;
        this.alertEventRepository = alertEventRepository;
    }

    public ReplayEventResponse getEventSeries(String roomId, int displayHours) {
        ReplayState state = sessionService.getState(roomId);
        int bucketMinutes = resolveBucketMinutes(displayHours);
        LocalDateTime[] range = resolveRange(state, displayHours);
        Map<String, Map<LocalDateTime, Integer>> operationSeries = operationEventRepository.aggregateOperationEvents(state.getUnitNo().intValue(), range[0], range[1], bucketMinutes);
        Map<LocalDateTime, Integer> alertSeries = alertEventRepository.aggregateAlertEvents(state.getUnitNo().intValue(), range[0], range[1], bucketMinutes);
        Map<String, Map<String, Integer>> result = new LinkedHashMap<String, Map<String, Integer>>();
        result.put("VDU1", convert(operationSeries.get("VDU1")));
        result.put("VDU2", convert(operationSeries.get("VDU2")));
        result.put("VDU3", convert(operationSeries.get("VDU3")));
        result.put("VDU4", convert(operationSeries.get("VDU4")));
        result.put("ALERT", convert(alertSeries));
        ReplayEventResponse response = new ReplayEventResponse();
        response.setSeries(result);
        return response;
    }

    private LocalDateTime[] resolveRange(ReplayState state, int displayHours) {
        if (state.getReplayMode() == ReplayMode.REALTIME) {
            LocalDateTime end = state.getCurrentReplayTime();
            LocalDateTime dayStart = end.toLocalDate().atStartOfDay();
            LocalDateTime start = end.minusHours(displayHours);
            if (start.isBefore(dayStart)) {
                start = dayStart;
            }
            return new LocalDateTime[] { start, end };
        }
        LocalDateTime base = state.getStartDateTime();
        LocalDateTime current = state.getCurrentReplayTime();
        long windowSeconds = displayHours * 3600L;
        long elapsed = Math.max(0L, Duration.between(base, current).getSeconds());
        long windowIndex = elapsed / windowSeconds;
        LocalDateTime start = base.plusSeconds(windowIndex * windowSeconds);
        LocalDateTime end = start.plusHours(displayHours);
        return new LocalDateTime[] { start, end };
    }

    private Map<String, Integer> convert(Map<LocalDateTime, Integer> source) {
        Map<String, Integer> converted = new LinkedHashMap<String, Integer>();
        if (source == null) return converted;
        for (Map.Entry<LocalDateTime, Integer> entry : source.entrySet()) {
            converted.put(entry.getKey().format(KEY_FORMAT), entry.getValue());
        }
        return converted;
    }

    private int resolveBucketMinutes(int displayHours) {
        switch (displayHours) {
            case 4: return 1;
            case 12: return 3;
            case 24: return 6;
            default: throw new IllegalArgumentException("displayHours must be 4, 12, or 24.");
        }
    }
}
