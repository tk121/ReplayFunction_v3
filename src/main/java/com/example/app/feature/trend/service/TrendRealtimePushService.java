package com.example.app.feature.trend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.app.feature.replay.common.model.ReplayState;
import com.example.app.feature.replay.common.service.ReplaySessionService;
import com.example.app.feature.replay.graphic.service.TrendProcessService;
import com.example.app.feature.trend.dto.TrendDeltaResponse;
import com.example.app.feature.trend.model.TrendSubscription;
import com.example.app.feature.trend.ws.TrendWsHub;

public class TrendRealtimePushService {

    private final ReplaySessionService replaySessionService;
    private final TrendRealtimeSessionService trendRealtimeSessionService;
    private final TrendRealtimeQueryService trendRealtimeQueryService;
    private final TrendWsHub trendWsHub;
    private final TrendProcessService trendProcessService;

    public TrendRealtimePushService(
            ReplaySessionService replaySessionService,
            TrendRealtimeSessionService trendRealtimeSessionService,
            TrendRealtimeQueryService trendRealtimeQueryService,
            TrendWsHub trendWsHub,
            TrendProcessService trendProcessService) {
        this.replaySessionService = replaySessionService;
        this.trendRealtimeSessionService = trendRealtimeSessionService;
        this.trendRealtimeQueryService = trendRealtimeQueryService;
        this.trendWsHub = trendWsHub;
        this.trendProcessService = trendProcessService;
    }

    public void onReplayTimeAdvanced() throws Exception {
        ReplayState sharedState = replaySessionService.getState("replayMode");
        LocalDateTime sharedReplayTime = sharedState.getCurrentReplayTime();

        if (sharedReplayTime == null) {
            return;
        }

        List<TrendSubscription> subscriptions =
                trendRealtimeSessionService.findAllRealtimeSubscriptions();

        if (subscriptions.isEmpty()) {
            return;
        }

        Map<Long, TrendDeltaResponse> deltaMap =
                trendRealtimeQueryService.loadDeltasByTrend(subscriptions, sharedReplayTime);

        for (TrendSubscription subscription : subscriptions) {
            if (subscription == null || subscription.getTrendId() == null) {
                continue;
            }

            TrendDeltaResponse delta = deltaMap.get(subscription.getTrendId());
            if (delta == null) {
                continue;
            }

            trendWsHub.sendDelta(subscription, delta);
            subscription.setLastDeliveredTime(sharedReplayTime);
        }

        // 必要なら trend 外部プロセスへ通知
        // trendProcessService.execute("REPLAY", sharedReplayTime.toString());
    }
}