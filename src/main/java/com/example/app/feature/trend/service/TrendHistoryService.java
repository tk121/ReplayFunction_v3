package com.example.app.feature.trend.service;

import java.time.LocalDate;

import com.example.app.feature.trend.dto.TrendHistoryResponse;
import com.example.app.feature.trend.model.TrendDefinition;
import com.example.app.feature.trend.repository.TrendDataRepository;
import com.example.app.feature.trend.repository.TrendDefinitionRepository;

public class TrendHistoryService {

    private final TrendDefinitionRepository trendDefinitionRepository;
    private final TrendDataRepository trendDataRepository;

    public TrendHistoryService(
            TrendDefinitionRepository trendDefinitionRepository,
            TrendDataRepository trendDataRepository) {
        this.trendDefinitionRepository = trendDefinitionRepository;
        this.trendDataRepository = trendDataRepository;
    }

    public TrendHistoryResponse loadOneDay(Long trendId, LocalDate targetDate, String loginUserId) {
        if (trendId == null) {
            throw new IllegalArgumentException("trendId は必須です");
        }
        if (targetDate == null) {
            throw new IllegalArgumentException("targetDate は必須です");
        }

        TrendDefinition definition = trendDefinitionRepository.findByTrendId(trendId);
        if (definition == null) {
            throw new IllegalArgumentException("trend が存在しません。trendId=" + trendId);
        }

        if (loginUserId == null || !loginUserId.equals(definition.getUserId())) {
            throw new IllegalStateException("この trend を参照する権限がありません");
        }

        TrendHistoryResponse response = new TrendHistoryResponse();
        response.setTrendId(definition.getTrendId());
        response.setTrendName(definition.getTrendName());
        response.setTargetDate(targetDate.toString());
        response.setSeries(
                trendDataRepository.findHistorySeriesByDevices(
                        definition.getDeviceIds(),
                        targetDate));
        return response;
    }
}