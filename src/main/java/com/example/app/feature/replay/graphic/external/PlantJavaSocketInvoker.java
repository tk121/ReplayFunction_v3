package com.example.app.feature.replay.graphic.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.example.app.feature.replay.graphic.external.plant.PlantAcceptedResponse;
import com.example.app.feature.replay.graphic.external.plant.PlantAsyncRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Java プロセスの Plant サーバへ Socket 通信で接続する実装です。
 * 
 * <p>
 * JSON形式でリクエスト/レスポンスを送受信します。
 * 改行区切りのテキストプロトコルを使用します。
 * </p>
 */
public class PlantJavaSocketInvoker implements ExternalInvoker<PlantAsyncRequest, PlantAcceptedResponse> {

    private final String host;
    private final int port;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlantJavaSocketInvoker(String host, int port, int connectTimeoutMillis, int readTimeoutMillis) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    @Override
    public PlantAcceptedResponse execute(PlantAsyncRequest request) throws Exception {
        Socket socket = new Socket();
        try {
            // Socket に接続
            socket.connect(
                new InetSocketAddress(host, port),
                connectTimeoutMillis);
            socket.setSoTimeout(readTimeoutMillis);

            // JSON でリクエスト送信
            String jsonRequest = objectMapper.writeValueAsString(request);
            OutputStream out = socket.getOutputStream();
            out.write((jsonRequest + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();

            // JSON でレスポンス受信
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String jsonResponse = reader.readLine();

            if (jsonResponse == null) {
                throw new IOException("Socket connection closed by server without response");
            }

            return objectMapper.readValue(jsonResponse, PlantAcceptedResponse.class);

        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to communicate with Plant Java server at " + host + ":" + port, e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
