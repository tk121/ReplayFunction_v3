package com.example.app.feature.replay.common.process;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

public class ExternalProcessConfigLoader {

    public List<ExternalProcessDefinition> load(ServletContext application) {
        List<ExternalProcessDefinition> definitions = new ArrayList<ExternalProcessDefinition>();

        boolean enabled = Boolean.parseBoolean(
                getInitParam(application, "process.enabled", "false"));

        if (!enabled) {
            return definitions;
        }

        String rawDefinitions = getInitParam(application, "process.definitions", "");
        if (rawDefinitions.length() == 0) {
            return definitions;
        }

        String[] names = rawDefinitions.split(",");
        for (String rawName : names) {
            String name = rawName == null ? "" : rawName.trim();
            if (name.length() == 0) {
                continue;
            }

            String prefix = "process." + name + ".";

            ExternalProcessDefinition def = new ExternalProcessDefinition();
            def.setName(name);
            def.setTransport(getInitParam(application, prefix + "transport", "socket"));
            def.setJarPath(getInitParam(application, prefix + "jar", ""));
            def.setCount(parseInt(getInitParam(application, prefix + "count", "1"), 1));
            def.setHost(getInitParam(application, prefix + "host", "127.0.0.1"));
            def.setPortStart(parseInt(getInitParam(application, prefix + "port.start", "0"), 0));
            def.setFifoDir(getInitParam(application, prefix + "fifo.dir", ""));
            def.setJvmOpts(getInitParam(application, prefix + "jvmOpts", ""));
            def.setArgs(getInitParam(application, prefix + "args", ""));
            def.setStartupWaitMillis(parseLong(
                    getInitParam(application, prefix + "startupWaitMillis", "1000"), 1000L));

            validate(def);
            definitions.add(def);
        }

        return definitions;
    }

    private void validate(ExternalProcessDefinition def) {
        if (def.getJarPath() == null || def.getJarPath().trim().length() == 0) {
            throw new IllegalArgumentException("jar path is required. process=" + def.getName());
        }
        if (def.getCount() <= 0) {
            throw new IllegalArgumentException("count must be > 0. process=" + def.getName());
        }
        if (def.isSocket()) {
            if (def.getPortStart() <= 0) {
                throw new IllegalArgumentException("port.start must be > 0. process=" + def.getName());
            }
        } else if (def.isFifo()) {
            if (def.getFifoDir() == null || def.getFifoDir().trim().length() == 0) {
                throw new IllegalArgumentException("fifo.dir is required. process=" + def.getName());
            }
        } else {
            throw new IllegalArgumentException("unsupported transport. process="
                    + def.getName() + ", transport=" + def.getTransport());
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getInitParam(ServletContext application, String key, String defaultValue) {
        String value = application.getInitParameter(key);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        return value.trim();
    }
}
