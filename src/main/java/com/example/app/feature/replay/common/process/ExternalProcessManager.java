package com.example.app.feature.replay.common.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalProcessManager {

    private static final Logger log = LoggerFactory.getLogger(ExternalProcessManager.class);

    private final List<ExternalProcessHandle> handles = new ArrayList<ExternalProcessHandle>();

    public synchronized void startAll(List<ExternalProcessDefinition> definitions) throws Exception {
        for (ExternalProcessDefinition def : definitions) {
            start(def);
        }
    }

    public synchronized void start(ExternalProcessDefinition def) throws Exception {
        for (int i = 0; i < def.getCount(); i++) {
            ExternalProcessHandle handle = startSingle(def, i);
            handles.add(handle);

            if (def.getStartupWaitMillis() > 0L) {
                Thread.sleep(def.getStartupWaitMillis());
            }
        }
    }

    public synchronized List<ExternalProcessHandle> getHandlesByName(String name) {
        List<ExternalProcessHandle> result = new ArrayList<ExternalProcessHandle>();
        for (ExternalProcessHandle handle : handles) {
            if (name.equals(handle.getName())) {
                result.add(handle);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<ExternalProcessHandle> getAllHandles() {
        return Collections.unmodifiableList(new ArrayList<ExternalProcessHandle>(handles));
    }

    public synchronized void shutdownAll() {
        for (ExternalProcessHandle handle : handles) {
            shutdownOne(handle);
        }
        handles.clear();
    }

    private ExternalProcessHandle startSingle(ExternalProcessDefinition def, int index) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add("java");

        addSplitTokens(command, def.getJvmOpts());

        command.add("-jar");
        command.add(def.getJarPath());

        String host = def.getHost();
        Integer port = null;
        String requestFifoPath = null;
        String responseFifoPath = null;

        if (def.isSocket()) {
            port = Integer.valueOf(def.getPortStart() + index);
            command.add("--host=" + host);
            command.add("--port=" + port.intValue());
        } else if (def.isFifo()) {
            requestFifoPath = buildRequestFifoPath(def, index);
            responseFifoPath = buildResponseFifoPath(def, index);

            ensureParentDirectory(def.getFifoDir());
            recreateFifo(requestFifoPath);
            recreateFifo(responseFifoPath);

            command.add("--requestFifo=" + requestFifoPath);
            command.add("--responseFifo=" + responseFifoPath);
        }

        addSplitTokens(command, def.getArgs());

        log.info("Starting external process. name={}, index={}, command={}",
                def.getName(), Integer.valueOf(index), command);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StreamGobbler.start(process.getInputStream(),
                def.getName() + "-" + index);

        ExternalProcessHandle handle = new ExternalProcessHandle(
                def.getName(),
                index,
                def.getTransport(),
                host,
                port,
                requestFifoPath,
                responseFifoPath,
                process);

        log.info("Started external process. {}", handle);
        return handle;
    }

    private void shutdownOne(ExternalProcessHandle handle) {
        try {
            Process process = handle.getProcess();
            if (process == null) {
                return;
            }

            log.info("Stopping external process. {}", handle);
            process.destroy();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (process.isAlive()) {
                process.destroyForcibly();
            }
        } catch (Exception e) {
            log.warn("Failed to stop external process. handle={}", handle, e);
        }
    }

    private void ensureParentDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created && !dir.exists()) {
                throw new IllegalStateException("failed to create fifo directory: " + dirPath);
            }
        }
    }

    private void recreateFifo(String fifoPath) throws IOException, InterruptedException {
        File fifo = new File(fifoPath);
        if (fifo.exists()) {
            boolean deleted = fifo.delete();
            if (!deleted && fifo.exists()) {
                throw new IllegalStateException("failed to delete existing fifo: " + fifoPath);
            }
        }

        ProcessBuilder pb = new ProcessBuilder("mkfifo", fifoPath);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StreamGobbler.start(process.getInputStream(), "mkfifo");

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("mkfifo failed. path=" + fifoPath + ", exitCode=" + exitCode);
        }
    }

    private String buildRequestFifoPath(ExternalProcessDefinition def, int index) {
        return def.getFifoDir() + File.separator + def.getName() + "_" + index + "_req";
    }

    private String buildResponseFifoPath(ExternalProcessDefinition def, int index) {
        return def.getFifoDir() + File.separator + def.getName() + "_" + index + "_res";
    }

    private void addSplitTokens(List<String> command, String raw) {
        if (raw == null) {
            return;
        }
        String trimmed = raw.trim();
        if (trimmed.length() == 0) {
            return;
        }

        String[] tokens = trimmed.split("\\s+");
        for (String token : tokens) {
            if (token != null && token.trim().length() > 0) {
                command.add(token.trim());
            }
        }
    }
}