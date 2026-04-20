package com.example.app.feature.trend.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.example.app.feature.trend.dto.TrendSaveRequest;
import com.example.app.feature.trend.model.TrendDefinition;
import com.example.app.feature.trend.repository.TrendDefinitionRepository;

public class TrendDefinitionService {

    private final TrendDefinitionRepository trendDefinitionRepository;

    public TrendDefinitionService(TrendDefinitionRepository trendDefinitionRepository) {
        this.trendDefinitionRepository = trendDefinitionRepository;
    }

    public List<TrendDefinition> findByUserId(String userId) {
        return trendDefinitionRepository.findByUserId(userId);
    }

    public TrendDefinition findByTrendId(Long trendId) {
        return trendDefinitionRepository.findByTrendId(trendId);
    }

    public TrendDefinition save(String loginUserId, TrendSaveRequest request) {
        if (loginUserId == null || loginUserId.trim().length() == 0) {
            throw new IllegalStateException("ログインユーザが不正です");
        }
        if (request == null) {
            throw new IllegalArgumentException("リクエストがありません");
        }

        String trendName = normalizeTrendName(request.getTrendName());
        List<String> deviceIds = normalizeDeviceIds(request.getDeviceIds());

        if (request.getTrendId() == null) {
            return trendDefinitionRepository.insert(loginUserId, trendName, deviceIds);
        }

        TrendDefinition current = trendDefinitionRepository.findByTrendId(request.getTrendId());
        if (current == null) {
            throw new IllegalArgumentException("更新対象の trend が存在しません");
        }
        if (!loginUserId.equals(current.getUserId())) {
            throw new IllegalStateException("この trend を更新する権限がありません");
        }

        return trendDefinitionRepository.update(
                request.getTrendId(),
                loginUserId,
                trendName,
                deviceIds);
    }

    public void delete(String loginUserId, Long trendId) {
        if (loginUserId == null || loginUserId.trim().length() == 0) {
            throw new IllegalStateException("ログインユーザが不正です");
        }
        if (trendId == null) {
            throw new IllegalArgumentException("trendId は必須です");
        }

        TrendDefinition current = trendDefinitionRepository.findByTrendId(trendId);
        if (current == null) {
            throw new IllegalArgumentException("削除対象の trend が存在しません");
        }
        if (!loginUserId.equals(current.getUserId())) {
            throw new IllegalStateException("この trend を削除する権限がありません");
        }

        trendDefinitionRepository.delete(trendId, loginUserId);
    }

    private String normalizeTrendName(String trendName) {
        if (trendName == null || trendName.trim().length() == 0) {
            throw new IllegalArgumentException("trendName は必須です");
        }
        return trendName.trim();
    }

    private List<String> normalizeDeviceIds(List<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            throw new IllegalArgumentException("deviceIds は1件以上必要です");
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        for (String deviceId : deviceIds) {
            if (deviceId == null) {
                continue;
            }
            String trimmed = deviceId.trim();
            if (trimmed.length() == 0) {
                continue;
            }
            normalized.add(trimmed);
        }

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("有効な deviceId がありません");
        }
        if (normalized.size() > 10) {
            throw new IllegalArgumentException("device は最大10件までです");
        }

        return new ArrayList<String>(normalized);
    }
}