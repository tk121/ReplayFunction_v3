package com.example.app.feature.trend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.example.app.feature.trend.dto.TrendDeltaResponse;
import com.example.app.feature.trend.model.TrendDataPoint;
import com.example.app.feature.trend.model.TrendDefinition;
import com.example.app.feature.trend.model.TrendSubscription;
import com.example.app.feature.trend.repository.TrendDataRepository;
import com.example.app.feature.trend.repository.TrendDefinitionRepository;

public class TrendRealtimeQueryService {

    private final TrendDefinitionRepository trendDefinitionRepository;
    private final TrendDataRepository trendDataRepository;

    public TrendRealtimeQueryService(
            TrendDefinitionRepository trendDefinitionRepository,
            TrendDataRepository trendDataRepository) {
        this.trendDefinitionRepository = trendDefinitionRepository;
        this.trendDataRepository = trendDataRepository;
    }

    /**
     * 購読中 trend 群について、shared replay time までの差分を trendId ごとに返します。
     *
     * <p>
     * 取得自体は device 集合でまとめて行い、
     * 取得結果を trendId ごとに振り分けます。
     * </p>
     */
    public Map<Long, TrendDeltaResponse> loadDeltasByTrend(
            List<TrendSubscription> subscriptions,
            LocalDateTime toInclusive) {

        Map<Long, TrendDefinition> definitionMap = new LinkedHashMap<Long, TrendDefinition>();
        LinkedHashSet<String> requiredDevices = new LinkedHashSet<String>();

        LocalDateTime minFromExclusive = null;

        for (TrendSubscription subscription : subscriptions) {
            if (subscription == null || subscription.getTrendId() == null) {
                continue;
            }

            TrendDefinition definition = definitionMap.get(subscription.getTrendId());
            if (definition == null) {
                definition = trendDefinitionRepository.findByTrendId(subscription.getTrendId());
                if (definition != null) {
                    definitionMap.put(subscription.getTrendId(), definition);
                    requiredDevices.addAll(definition.getDeviceIds());
                }
            }

            if (subscription.getLastDeliveredTime() != null) {
                if (minFromExclusive == null
                        || subscription.getLastDeliveredTime().isBefore(minFromExclusive)) {
                    minFromExclusive = subscription.getLastDeliveredTime();
                }
            }
        }

        Map<Long, TrendDeltaResponse> result =
                new LinkedHashMap<Long, TrendDeltaResponse>();

        if (definitionMap.isEmpty() || requiredDevices.isEmpty()) {
            return result;
        }

        if (minFromExclusive == null || toInclusive == null || !toInclusive.isAfter(minFromExclusive)) {
            for (Map.Entry<Long, TrendDefinition> entry : definitionMap.entrySet()) {
                TrendDeltaResponse empty = new TrendDeltaResponse();
                empty.setTrendId(entry.getKey());
                for (String deviceId : entry.getValue().getDeviceIds()) {
                    empty.ensureDevice(deviceId);
                }
                result.put(entry.getKey(), empty);
            }
            return result;
        }

        List<TrendDataPoint> rawPoints =
                trendDataRepository.findRealtimeDeltaByDevices(
                        new ArrayList<String>(requiredDevices),
                        minFromExclusive,
                        toInclusive);

        for (Map.Entry<Long, TrendDefinition> entry : definitionMap.entrySet()) {
            Long trendId = entry.getKey();
            TrendDefinition definition = entry.getValue();

            TrendDeltaResponse response = new TrendDeltaResponse();
            response.setTrendId(trendId);

            for (String deviceId : definition.getDeviceIds()) {
                response.ensureDevice(deviceId);
            }

            for (TrendDataPoint point : rawPoints) {
                if (definition.getDeviceIds().contains(point.getDeviceId())) {
                    response.getSeries().get(point.getDeviceId()).add(point);
                }
            }

            result.put(trendId, response);
        }

        return result;
    }
}