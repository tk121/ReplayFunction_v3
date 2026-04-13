package com.example.app.feature.replay.common.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamGobbler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(StreamGobbler.class);

    private final InputStream inputStream;
    private final String name;

    private StreamGobbler(InputStream inputStream, String name) {
        this.inputStream = inputStream;
        this.name = name;
    }

    public static void start(InputStream inputStream, String name) {
        Thread thread = new Thread(new StreamGobbler(inputStream, name));
        thread.setDaemon(true);
        thread.setName("process-log-" + name);
        thread.start();
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[external:{}] {}", name, line);
            }
        } catch (IOException e) {
            log.warn("Failed to read process output. name={}", name, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.debug("reader close failed", e);
                }
            }
        }
    }
}
