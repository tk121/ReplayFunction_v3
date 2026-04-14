package com.example.app.feature.replay.common.listener;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.common.datasource.DataSourceProvider;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.repository.UserRepository;
import com.example.app.feature.auth.service.AuthService;
import com.example.app.feature.replay.common.controller.ws.WsHub;
import com.example.app.feature.replay.common.engine.ReplayEngine;
import com.example.app.feature.replay.common.process.ExternalProcessConfigLoader;
import com.example.app.feature.replay.common.process.ExternalProcessDefinition;
import com.example.app.feature.replay.common.process.ExternalProcessHandle;
import com.example.app.feature.replay.common.process.ExternalProcessManager;
import com.example.app.feature.replay.common.processclient.ExternalClientPool;
import com.example.app.feature.replay.common.processclient.ExternalClientPoolFactory;
import com.example.app.feature.replay.common.service.ReplayResponseService;
import com.example.app.feature.replay.common.service.ReplaySessionService;
import com.example.app.feature.replay.event.repository.AlertCountPerMinuteRepository;
import com.example.app.feature.replay.event.repository.VduOperationCountPerMinuteRepository;
import com.example.app.feature.replay.event.service.ReplayEventService;
import com.example.app.feature.replay.graphic.dto.external.GraphicProcessRequest;
import com.example.app.feature.replay.graphic.dto.external.GraphicProcessResponse;
import com.example.app.feature.replay.graphic.dto.external.TrendProcessRequest;
import com.example.app.feature.replay.graphic.dto.external.TrendProcessResponse;
import com.example.app.feature.replay.graphic.mapper.AlertLogMapper;
import com.example.app.feature.replay.graphic.mapper.OperationLogMapper;
import com.example.app.feature.replay.graphic.repository.AlertLogRepository;
import com.example.app.feature.replay.graphic.repository.OperationLogRepository;
import com.example.app.feature.replay.graphic.repository.PlantDataLogRepository;
import com.example.app.feature.replay.graphic.service.GraphicProcessService;
import com.example.app.feature.replay.graphic.service.ReplayCoordinator;
import com.example.app.feature.replay.graphic.service.ReplayExternalProcessService;
import com.example.app.feature.replay.graphic.service.TrendProcessService;

@WebListener
public class AppInitListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppInitListener.class);

    private ExternalProcessManager externalProcessManager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            log.info("contextInitialized start");

            ServletContext application = sce.getServletContext();

            String jndi = getInitParam(application, "replay.jndi", "java:comp/env/jdbc/mydb");
            DataSource ds = DataSourceProvider.lookup(jndi);

            WsHub wsHub = new WsHub();
            ReplaySessionService sessionService = new ReplaySessionService();
            ReplayResponseService responseService = new ReplayResponseService(sessionService);

            UserRepository userRepository = new UserRepository(ds);
            AuthService authService = new AuthService(userRepository);

            OperationLogRepository operationLogRepository = new OperationLogRepository(ds);
            AlertLogRepository alertLogRepository = new AlertLogRepository(ds);
            PlantDataLogRepository plantDataLogRepository = new PlantDataLogRepository(ds);

            OperationLogMapper operationMapper = new OperationLogMapper();
            AlertLogMapper alertLogMapper = new AlertLogMapper();

            // 外部常駐プロセス起動
            externalProcessManager = new ExternalProcessManager();
            ExternalProcessConfigLoader configLoader = new ExternalProcessConfigLoader();
            List<ExternalProcessDefinition> definitions = configLoader.load(application);
            externalProcessManager.startAll(definitions);

            // graphic 用ハンドル取得
            List<ExternalProcessHandle> graphicHandles =
                    externalProcessManager.getHandlesByName("graphic");

            if (graphicHandles == null || graphicHandles.isEmpty()) {
                throw new IllegalStateException(
                        "No external process handles found for 'graphic'. "
                        + "Check process.enabled / process.definitions / process.graphic.* settings.");
            }

            // trend 用ハンドル取得
            List<ExternalProcessHandle> trendHandles =
                    externalProcessManager.getHandlesByName("trend");

            if (trendHandles == null || trendHandles.isEmpty()) {
                throw new IllegalStateException(
                        "No external process handles found for 'trend'. "
                        + "Check process.enabled / process.definitions / process.trend.* settings.");
            }

            ExternalClientPoolFactory poolFactory = new ExternalClientPoolFactory();

            // graphic 用 pool
            ExternalClientPool<GraphicProcessRequest, GraphicProcessResponse> graphicPool =
                    poolFactory.createPool(
                            graphicHandles,
                            GraphicProcessResponse.class,
                            parseInt(getInitParam(
                                    application,
                                    "process.graphic.connectTimeoutMillis",
                                    "3000"), 3000),
                            parseInt(getInitParam(
                                    application,
                                    "process.graphic.readTimeoutMillis",
                                    "3000"), 3000));

            // trend 用 pool
            ExternalClientPool<TrendProcessRequest, TrendProcessResponse> trendPool =
                    poolFactory.createPool(
                            trendHandles,
                            TrendProcessResponse.class,
                            parseInt(getInitParam(
                                    application,
                                    "process.trend.connectTimeoutMillis",
                                    "3000"), 3000),
                            parseInt(getInitParam(
                                    application,
                                    "process.trend.readTimeoutMillis",
                                    "3000"), 3000));

            GraphicProcessService graphicProcessService =
                    new GraphicProcessService(graphicPool);

            TrendProcessService trendProcessService =
                    new TrendProcessService(trendPool);

            ReplayExternalProcessService externalProcessService =
                    new ReplayExternalProcessService(
                            graphicProcessService,
                            trendProcessService);

            ReplayEventService eventService = new ReplayEventService(
                    sessionService,
                    new VduOperationCountPerMinuteRepository(ds),
                    new AlertCountPerMinuteRepository(ds));

            ReplayCoordinator coordinator = new ReplayCoordinator(
                    sessionService,
                    responseService,
                    wsHub,
                    operationLogRepository,
                    alertLogRepository,
                    plantDataLogRepository,
                    operationMapper,
                    alertLogMapper,
                    externalProcessService,
                    eventService);

            ReplayEngine engine = new ReplayEngine(
                    sessionService,
                    coordinator,
                    responseService,
                    wsHub);

            AppRuntime.initializeReplay(
                    ds,
                    wsHub,
                    sessionService,
                    responseService,
                    engine,
                    coordinator,
                    authService,
                    eventService);

            engine.start();
            log.info("contextInitialized end");

        } catch (Exception e) {
            log.error("AppInitListener failed", e);

            try {
                if (externalProcessManager != null) {
                    externalProcessManager.shutdownAll();
                }
            } catch (Exception shutdownEx) {
                log.warn("Failed to shutdown external processes after init error", shutdownEx);
            }

            throw new RuntimeException("AppInitListener failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (AppRuntime.getReplayEngine() != null) {
                AppRuntime.getReplayEngine().shutdown();
            }
        } catch (Exception e) {
            log.warn("Failed to shutdown ReplayEngine", e);
        }

        try {
            if (externalProcessManager != null) {
                externalProcessManager.shutdownAll();
            }
        } catch (Exception e) {
            log.warn("Failed to shutdown external processes", e);
        }
    }

    private String getInitParam(ServletContext application, String key, String defaultValue) {
        String value = application.getInitParameter(key);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        return value.trim();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}