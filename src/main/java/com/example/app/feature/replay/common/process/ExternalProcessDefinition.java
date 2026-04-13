package com.example.app.feature.replay.common.process;

public class ExternalProcessDefinition {

    public static final String TRANSPORT_SOCKET = "socket";
    public static final String TRANSPORT_FIFO = "fifo";

    private String name;
    private String transport;
    private String jarPath;
    private int count;
    private String host;
    private int portStart;
    private String fifoDir;
    private String jvmOpts;
    private String args;
    private long startupWaitMillis;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPortStart() {
        return portStart;
    }

    public void setPortStart(int portStart) {
        this.portStart = portStart;
    }

    public String getFifoDir() {
        return fifoDir;
    }

    public void setFifoDir(String fifoDir) {
        this.fifoDir = fifoDir;
    }

    public String getJvmOpts() {
        return jvmOpts;
    }

    public void setJvmOpts(String jvmOpts) {
        this.jvmOpts = jvmOpts;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public long getStartupWaitMillis() {
        return startupWaitMillis;
    }

    public void setStartupWaitMillis(long startupWaitMillis) {
        this.startupWaitMillis = startupWaitMillis;
    }

    public boolean isSocket() {
        return TRANSPORT_SOCKET.equalsIgnoreCase(transport);
    }

    public boolean isFifo() {
        return TRANSPORT_FIFO.equalsIgnoreCase(transport);
    }
}
