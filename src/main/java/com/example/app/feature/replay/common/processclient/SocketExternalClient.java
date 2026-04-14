package com.example.app.feature.replay.common.processclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.example.app.common.json.JsonUtil;

public class SocketExternalClient<REQ, RES> implements ExternalClient<REQ, RES> {

    private final String host;
    private final int port;
    private final Class<RES> responseType;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    public SocketExternalClient(
            String host,
            int port,
            Class<RES> responseType,
            int connectTimeoutMillis,
            int readTimeoutMillis) {
        this.host = host;
        this.port = port;
        this.responseType = responseType;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    @Override
    public RES send(REQ request) throws Exception {
        Socket socket = new Socket();
        try {
            socket.connect(new java.net.InetSocketAddress(host, port), connectTimeoutMillis);
            socket.setSoTimeout(readTimeoutMillis);

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));

            String requestJson = JsonUtil.writeValueAsString(request);
            writer.write(requestJson);
            writer.newLine();
            writer.flush();

            String responseJson = reader.readLine();
            if (responseJson == null) {
                throw new IllegalStateException("socket response is null. host=" + host + ", port=" + port);
            }

            return JsonUtil.readValue(responseJson, responseType);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
