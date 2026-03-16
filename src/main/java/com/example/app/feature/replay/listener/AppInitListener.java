package com.example.app.feature.replay.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import com.example.app.common.datasource.DataSourceProvider;
import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.c.CInvoker;
import com.example.app.feature.replay.c.ProcessBuilderCInvoker;
import com.example.app.feature.replay.c.SocketCInvoker;
import com.example.app.feature.replay.controller.ws.WsHub;
import com.example.app.feature.replay.engine.ReplayEngine;
import com.example.app.feature.replay.repository.EventLogRepository;
import com.example.app.feature.replay.service.ReplayControlConfig;
import com.example.app.feature.replay.service.ReplayCoordinator;
import com.example.app.feature.replay.service.ReplayResponseService;
import com.example.app.feature.replay.service.ReplaySessionService;

/**
 * アプリ起動・終了時の初期化処理を行う Listener です。
 *
 * <p>
 * replay 機能に必要な共有オブジェクトを生成し、
 * AppRuntime へ登録します。
 * また、ReplayEngine の起動と停止も担当します。
 * </p>
 */
@WebListener
public class AppInitListener implements ServletContextListener {

    /**
     * アプリケーション起動時に呼ばれます。
     *
     * <p>
     * DataSource、Service、Repository、WebSocket ハブ、
     * CInvoker、ReplayEngine などを生成し、
     * 最後に ReplayEngine を起動します。
     * </p>
     *
     * @param sce ServletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext application = sce.getServletContext();

            // JNDI から DataSource を取得
            String jndi = getInitParam(application, "replay.jndi", "java:comp/env/jdbc/mydb");
            DataSource ds = DataSourceProvider.lookup(jndi);
            
            // heartbeat 設定を読み込む
            boolean heartbeatEnabled = Boolean.parseBoolean(
                    getInitParam(application, "replay.control.heartbeat.enabled", "true"));
            int heartbeatTimeoutSeconds = Integer.parseInt(
                    getInitParam(application, "replay.control.heartbeat.timeoutSeconds", "30"));

            ReplayControlConfig controlConfig = new ReplayControlConfig(
                    heartbeatEnabled,
                    heartbeatTimeoutSeconds);

            // replay で利用する各コンポーネントを生成
            WsHub wsHub = new WsHub();
            ReplaySessionService sessionService = new ReplaySessionService(controlConfig);
            ReplayResponseService responseService = new ReplayResponseService(sessionService);
            EventLogRepository eventLogRepository = new EventLogRepository(ds);

            // C 呼び出し方式を設定値から決定
            CInvoker cInvoker = createCInvoker(application);

            ReplayCoordinator coordinator = new ReplayCoordinator(
                    sessionService,
                    responseService,
                    wsHub,
                    eventLogRepository,
                    cInvoker
            );

            ReplayEngine engine = new ReplayEngine(
                    sessionService,
                    coordinator,
                    responseService,
                    wsHub
            );

            // 生成した共有オブジェクトを AppRuntime に保持
            AppRuntime.initializeReplay(
                    ds,
                    wsHub,
                    sessionService,
                    responseService,
                    engine,
                    coordinator,
                    controlConfig
            );

            // replay エンジン起動
            engine.start();

        } catch (Exception e) {
            throw new RuntimeException("AppInitListener failed: " + e.getMessage(), e);
        }
    }

    /**
     * アプリケーション終了時に呼ばれます。
     *
     * <p>
     * 起動中の ReplayEngine を停止します。
     * </p>
     *
     * @param sce ServletContextEvent
     */
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
     * CInvoker 実装を設定値から生成します。
     *
     * <p>
     * replay.c.mode が socket なら SocketCInvoker、
     * それ以外は ProcessBuilderCInvoker を使用します。
     * </p>
     *
     * @param application ServletContext
     * @return CInvoker 実装
     */
    private CInvoker createCInvoker(ServletContext application) {
        String mode = getInitParam(application, "replay.c.mode", "process");

        if ("socket".equalsIgnoreCase(mode)) {
            String host = getInitParam(application, "replay.c.socket.host", "127.0.0.1");
            int port = Integer.parseInt(getInitParam(application, "replay.c.socket.port", "5001"));
            return new SocketCInvoker(host, port);
        }

        String command = getInitParam(application, "replay.c.process.command", "/opt/myapp/bin/replay_c_client");
        long timeoutMillis = Long.parseLong(getInitParam(application, "replay.c.process.timeoutMillis", "3000"));
        return new ProcessBuilderCInvoker(command, timeoutMillis);
    }

    /**
     * web.xml などの init-param を取得します。
     *
     * <p>
     * 未設定の場合は既定値を返します。
     * </p>
     *
     * @param application ServletContext
     * @param key パラメータ名
     * @param defaultValue 既定値
     * @return 設定値または既定値
     */
    private String getInitParam(ServletContext application, String key, String defaultValue) {
        String value = application.getInitParameter(key);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        return value.trim();
    }
}