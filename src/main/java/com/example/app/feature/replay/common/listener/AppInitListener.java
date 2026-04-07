package com.example.app.feature.replay.common.listener;

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
import com.example.app.feature.replay.common.service.ReplayResponseService;
import com.example.app.feature.replay.common.service.ReplaySessionService;
import com.example.app.feature.replay.event.repository.AlertCountPerMinuteRepository;
import com.example.app.feature.replay.event.repository.VduOperationCountPerMinuteRepository;
import com.example.app.feature.replay.event.service.ReplayEventService;
import com.example.app.feature.replay.graphic.c.CInvoker;
import com.example.app.feature.replay.graphic.c.LengthPrefixedSocketCInvoker;
import com.example.app.feature.replay.graphic.c.PlantJavaSocketInvoker;
import com.example.app.feature.replay.graphic.c.plant.PlantAcceptedResponse;
import com.example.app.feature.replay.graphic.c.plant.PlantAsyncRequest;
import com.example.app.feature.replay.graphic.mapper.AlertLogMapper;
import com.example.app.feature.replay.graphic.mapper.OperationLogMapper;
import com.example.app.feature.replay.graphic.repository.AlertLogRepository;
import com.example.app.feature.replay.graphic.repository.OperationLogRepository;
import com.example.app.feature.replay.graphic.repository.PlantDataLogRepository;
import com.example.app.feature.replay.graphic.service.PlantDataProcessService;
import com.example.app.feature.replay.graphic.service.ReplayCoordinator;
import com.example.app.feature.replay.graphic.service.ReplayExternalProcessService;

/**
 * アプリ起動・終了時の初期化処理を行う Listener です。
 */
@WebListener
public class AppInitListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppInitListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            log.info("contextInitialized start");

            ServletContext application = sce.getServletContext();

            // JNDI から DataSource を取得
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

            // Plant 用 非同期 C サーバ invoker
            CInvoker<PlantAsyncRequest, PlantAcceptedResponse> plantCInvoker =
                    createPlantAsyncSocketInvoker(application);

            // Plant 送信サービス
            PlantDataProcessService plantDataProcessService =
                    new PlantDataProcessService(plantCInvoker);

            // 外部 C プロセス連携の集約窓口
            ReplayExternalProcessService externalProcessService =
                    new ReplayExternalProcessService(plantDataProcessService);

            // event 集計済みテーブル用サービス
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
            // 必要ならログ出力
        }
    }

    /**
     * Plant 用 非同期サーバ invoker を生成します。
     * C プロセスまたは Java プロセスを切り替え可能です。
     */
    private CInvoker<PlantAsyncRequest, PlantAcceptedResponse> createPlantAsyncSocketInvoker(
            ServletContext application) {

        String host = getInitParam(application, "replay.plant.socket.host", "127.0.0.1");
        int port = Integer.parseInt(getInitParam(application, "replay.plant.socket.port", "5000"));
        int connectTimeoutMillis = Integer.parseInt(getInitParam(
                application, "replay.plant.socket.connectTimeoutMillis", "3000"));
        int readTimeoutMillis = Integer.parseInt(getInitParam(
                application, "replay.plant.socket.readTimeoutMillis", "3000"));

        // サーバ種別を判定: "c-process" または "java-process"
        String processType = getInitParam(application, "replay.plant.process.type", "c-process");

        if ("java-process".equals(processType)) {
            // Java プロセス用 (JSON通信)
            return new PlantJavaSocketInvoker(host, port, connectTimeoutMillis, readTimeoutMillis);
        } else {
            // C プロセス用 (既存のバイナリ通信)
            return new LengthPrefixedSocketCInvoker<PlantAsyncRequest, PlantAcceptedResponse>(
                    host, port, connectTimeoutMillis, readTimeoutMillis, PlantAcceptedResponse.class);
        }
    }

    /**
     * web.xml などの init-param を取得します。
     */
    private String getInitParam(ServletContext application, String key, String defaultValue) {
        String value = application.getInitParameter(key);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        return value.trim();
    }
}