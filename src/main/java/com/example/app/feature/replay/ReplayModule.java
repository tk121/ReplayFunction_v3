package com.example.app.feature.replay;

import javax.sql.DataSource;

import com.example.app.feature.replay.common.controller.ws.WsHub;
import com.example.app.feature.replay.common.engine.ReplayEngine;
import com.example.app.feature.replay.common.service.ReplayResponseService;
import com.example.app.feature.replay.common.service.ReplaySessionService;
import com.example.app.feature.replay.event.repository.AlertCountPerMinuteRepository;
import com.example.app.feature.replay.event.repository.VduOperationCountPerMinuteRepository;
import com.example.app.feature.replay.event.service.ReplayEventService;
import com.example.app.feature.replay.graphic.mapper.AlertLogMapper;
import com.example.app.feature.replay.graphic.mapper.OperationLogMapper;
import com.example.app.feature.replay.graphic.repository.AlertLogRepository;
import com.example.app.feature.replay.graphic.repository.OperationLogRepository;
import com.example.app.feature.replay.graphic.repository.PlantDataLogRepository;
import com.example.app.feature.replay.graphic.service.ReplayCoordinator;
import com.example.app.feature.replay.graphic.service.ReplayExternalProcessService;

public class ReplayModule {

    private final WsHub wsHub;
    private final ReplaySessionService replaySessionService;
    private final ReplayResponseService replayResponseService;
    private final ReplayEventService replayEventService;
    private final ReplayCoordinator replayCoordinator;
    private final ReplayEngine replayEngine;

    public ReplayModule(
            DataSource dataSource,
            ReplayExternalProcessService replayExternalProcessService) {

        this.wsHub = new WsHub();
        this.replaySessionService = new ReplaySessionService();
        this.replayResponseService = new ReplayResponseService(replaySessionService);

        OperationLogRepository operationLogRepository =
                new OperationLogRepository(dataSource);
        AlertLogRepository alertLogRepository =
                new AlertLogRepository(dataSource);
        PlantDataLogRepository plantDataLogRepository =
                new PlantDataLogRepository(dataSource);

        OperationLogMapper operationLogMapper = new OperationLogMapper();
        AlertLogMapper alertLogMapper = new AlertLogMapper();

        this.replayEventService = new ReplayEventService(
                replaySessionService,
                new VduOperationCountPerMinuteRepository(dataSource),
                new AlertCountPerMinuteRepository(dataSource));

        this.replayCoordinator = new ReplayCoordinator(
                replaySessionService,
                replayResponseService,
                wsHub,
                operationLogRepository,
                alertLogRepository,
                plantDataLogRepository,
                operationLogMapper,
                alertLogMapper,
                replayExternalProcessService,
                replayEventService);

        this.replayEngine = new ReplayEngine(
                replaySessionService,
                replayCoordinator,
                replayResponseService,
                wsHub);
    }

    public WsHub getWsHub() {
        return wsHub;
    }

    public ReplaySessionService getReplaySessionService() {
        return replaySessionService;
    }

    public ReplayResponseService getReplayResponseService() {
        return replayResponseService;
    }

    public ReplayEventService getReplayEventService() {
        return replayEventService;
    }

    public ReplayCoordinator getReplayCoordinator() {
        return replayCoordinator;
    }

    public ReplayEngine getReplayEngine() {
        return replayEngine;
    }
}
