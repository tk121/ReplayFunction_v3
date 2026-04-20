package com.example.app.feature.trend;

import javax.sql.DataSource;

import com.example.app.feature.replay.common.service.ReplaySessionService;
import com.example.app.feature.replay.graphic.service.TrendProcessService;
import com.example.app.feature.trend.repository.DeviceRepository;
import com.example.app.feature.trend.repository.TrendDataRepository;
import com.example.app.feature.trend.repository.TrendDefinitionRepository;
import com.example.app.feature.trend.service.DeviceService;
import com.example.app.feature.trend.service.TrendDefinitionService;
import com.example.app.feature.trend.service.TrendHistoryService;
import com.example.app.feature.trend.service.TrendRealtimePushService;
import com.example.app.feature.trend.service.TrendRealtimeQueryService;
import com.example.app.feature.trend.service.TrendRealtimeSessionService;
import com.example.app.feature.trend.ws.TrendWsHub;

public class TrendModule {

    private final DeviceService deviceService;
    private final TrendDefinitionService trendDefinitionService;
    private final TrendHistoryService trendHistoryService;
    private final TrendRealtimeSessionService trendRealtimeSessionService;
    private final TrendRealtimeQueryService trendRealtimeQueryService;
    private final TrendRealtimePushService trendRealtimePushService;
    private final TrendWsHub trendWsHub;
    private final TrendProcessService trendProcessService;

    public TrendModule(
            DataSource dataSource,
            ReplaySessionService replaySessionService,
            TrendProcessService trendProcessService) {

        DeviceRepository deviceRepository = new DeviceRepository(dataSource);
        TrendDefinitionRepository trendDefinitionRepository =
                new TrendDefinitionRepository(dataSource);
        TrendDataRepository trendDataRepository =
                new TrendDataRepository(dataSource);

        this.deviceService = new DeviceService(deviceRepository);

        this.trendDefinitionService =
                new TrendDefinitionService(trendDefinitionRepository);

        this.trendHistoryService =
                new TrendHistoryService(
                        trendDefinitionRepository,
                        trendDataRepository);

        this.trendRealtimeSessionService =
                new TrendRealtimeSessionService();

        this.trendRealtimeQueryService =
                new TrendRealtimeQueryService(
                        trendDefinitionRepository,
                        trendDataRepository);

        this.trendWsHub = new TrendWsHub();

        this.trendRealtimePushService =
                new TrendRealtimePushService(
                        replaySessionService,
                        trendRealtimeSessionService,
                        trendRealtimeQueryService,
                        trendWsHub,
                        trendProcessService);

        this.trendProcessService = trendProcessService;
    }

    public DeviceService getDeviceService() {
        return deviceService;
    }

    public TrendDefinitionService getTrendDefinitionService() {
        return trendDefinitionService;
    }

    public TrendHistoryService getTrendHistoryService() {
        return trendHistoryService;
    }

    public TrendRealtimeSessionService getTrendRealtimeSessionService() {
        return trendRealtimeSessionService;
    }

    public TrendRealtimePushService getTrendRealtimePushService() {
        return trendRealtimePushService;
    }

    public TrendWsHub getTrendWsHub() {
        return trendWsHub;
    }

    public TrendProcessService getTrendProcessService() {
        return trendProcessService;
    }
}