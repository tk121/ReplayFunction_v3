package com.example.app.feature.replay.common.process;

public class ExternalProcessHandle {

    private final String name;
    private final int instanceIndex;
    private final String transport;
    private final String host;
    private final Integer port;
    private final String requestFifoPath;
    private final String responseFifoPath;
    private final Process process;

    public ExternalProcessHandle(
            String name,
            int instanceIndex,
            String transport,
            String host,
            Integer port,
            String requestFifoPath,
            String responseFifoPath,
            Process process) {
        this.name = name;
        this.instanceIndex = instanceIndex;
        this.transport = transport;
        this.host = host;
        this.port = port;
        this.requestFifoPath = requestFifoPath;
        this.responseFifoPath = responseFifoPath;
        this.process = process;
    }

    public String getName() {
        return name;
    }

    public int getInstanceIndex() {
        return instanceIndex;
    }

    public String getTransport() {
        return transport;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getRequestFifoPath() {
        return requestFifoPath;
    }

    public String getResponseFifoPath() {
        return responseFifoPath;
    }

    public Process getProcess() {
        return process;
    }

    public boolean isSocket() {
        return ExternalProcessDefinition.TRANSPORT_SOCKET.equalsIgnoreCase(transport);
    }

    public boolean isFifo() {
        return ExternalProcessDefinition.TRANSPORT_FIFO.equalsIgnoreCase(transport);
    }

    @Override
    public String toString() {
        return "ExternalProcessHandle{"
                + "name='" + name + '\''
                + ", instanceIndex=" + instanceIndex
                + ", transport='" + transport + '\''
                + ", host='" + host + '\''
                + ", port=" + port
                + ", requestFifoPath='" + requestFifoPath + '\''
                + ", responseFifoPath='" + responseFifoPath + '\''
                + '}';
    }
}