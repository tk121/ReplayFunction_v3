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
import com.example.app.feature.auth.AuthModule;
import com.example.app.feature.replay.ReplayModule;
import com.example.app.feature.replay.common.process.ExternalProcessConfigLoader;
import com.example.app.feature.replay.common.process.ExternalProcessDefinition;
import com.example.app.feature.replay.common.process.ExternalProcessHandle;
import com.example.app.feature.replay.common.process.ExternalProcessManager;
import com.example.app.feature.replay.common.processclient.ExternalClientPool;
import com.example.app.feature.replay.common.processclient.ExternalClientPoolFactory;
import com.example.app.feature.replay.graphic.dto.external.GraphicProcessRequest;
import com.example.app.feature.replay.graphic.dto.external.GraphicProcessResponse;
import com.example.app.feature.replay.graphic.dto.external.TrendProcessRequest;
import com.example.app.feature.replay.graphic.dto.external.TrendProcessResponse;
import com.example.app.feature.replay.graphic.service.GraphicProcessService;
import com.example.app.feature.replay.graphic.service.ReplayExternalProcessService;
import com.example.app.feature.replay.graphic.service.TrendProcessService;
import com.example.app.feature.trend.TrendModule;

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

			externalProcessManager = new ExternalProcessManager();
			ExternalProcessConfigLoader configLoader = new ExternalProcessConfigLoader();
			List<ExternalProcessDefinition> definitions = configLoader.load(application);
			externalProcessManager.startAll(definitions);

			List<ExternalProcessHandle> graphicHandles = externalProcessManager.getHandlesByName("graphic");
			List<ExternalProcessHandle> trendHandles = externalProcessManager.getHandlesByName("trend");

			ExternalClientPoolFactory poolFactory = new ExternalClientPoolFactory();

			ExternalClientPool<GraphicProcessRequest, GraphicProcessResponse> graphicPool = poolFactory.createPool(
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

			ExternalClientPool<TrendProcessRequest, TrendProcessResponse> trendPool = poolFactory.createPool(
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

			GraphicProcessService graphicProcessService = new GraphicProcessService(graphicPool);
			TrendProcessService trendProcessService = new TrendProcessService(trendPool);

			ReplayExternalProcessService replayExternalProcessService = new ReplayExternalProcessService(
					graphicProcessService,
					trendProcessService);

			AuthModule authModule = new AuthModule(ds);
			ReplayModule replayModule = new ReplayModule(ds, replayExternalProcessService);
			TrendModule trendModule = new TrendModule(
					ds,
					replayModule.getReplaySessionService(),
					trendProcessService);

			AppRuntime.initialize(ds, authModule, replayModule, trendModule);

			replayModule.getReplayEngine().start();

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
			if (AppRuntime.getReplayModule() != null) {
				AppRuntime.getReplayModule().getReplayEngine().shutdown();
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