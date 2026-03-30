package com.example.app.feature.replay.graphic.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.app.feature.replay.graphic.entity.PlantDataLog;

public class PlantDataProcessService {

	private static final Logger log = LoggerFactory.getLogger(PlantDataProcessService.class);

	private final String command;

	private final long timeoutMillis;

	public PlantDataProcessService(String command, long timeoutMillis) {
		this.command = command;
		this.timeoutMillis = timeoutMillis;
	}

	/**
	 * plant_data_log 1件に対応する C プロセスを実行します。
	 *
	 * <p>
	 * 引数:
	 *   1: symbol
	 *   2: di_value
	 * </p>
	 */
	public void execute(PlantDataLog plantData) {
		if (plantData == null) {
			return;
		}

		if (plantData.getSymbol() == null || plantData.getSymbol().trim().length() == 0) {
			log.warn("Skip plant process because symbol is empty. dataId={}", plantData.getDataId());
			return;
		}
		if (plantData.getDiValue() == null) {
			log.warn("Skip plant process because diValue is null. dataId={}, symbol={}",
					plantData.getDataId(), plantData.getSymbol());
			return;
		}

		List<String> commands = new ArrayList<String>();
		commands.add(command);
		commands.add(plantData.getSymbol());
		commands.add(String.valueOf(plantData.getDiValue().intValue()));

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);

		Process process = null;

		try {
			log.info("Execute plant process. dataId={}, occurredAt={}, symbol={}, diValue={}",
					plantData.getDataId(),
					plantData.getOccurredAt(),
					plantData.getSymbol(),
					plantData.getDiValue());

			process = pb.start();

			boolean finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
			if (!finished) {
				process.destroyForcibly();
				throw new RuntimeException("plant process timeout");
			}

			int exitCode = process.exitValue();

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

				String line;
				while ((line = reader.readLine()) != null) {
					log.info("[plant-c] {}", line);
				}
			}

			if (exitCode != 0) {
				throw new RuntimeException("plant process failed. exitCode=" + exitCode);
			}

		} catch (Exception e) {
			log.error("Failed to execute plant process. dataId={}, symbol={}, diValue={}",
					plantData.getDataId(),
					plantData.getSymbol(),
					plantData.getDiValue(),
					e);
			throw new RuntimeException("plant process 実行失敗", e);
		}
	}
}
