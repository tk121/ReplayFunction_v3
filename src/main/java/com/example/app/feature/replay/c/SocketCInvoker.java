package com.example.app.feature.replay.c;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;

import com.example.app.common.json.JsonUtil;

/**
 * ソケット通信で常駐 C プロセスへ依頼する実装です。
 *
 * <p>
 * 常駐 C サーバへ TCP 接続し、
 * JSON を1回送って1回応答を受け取る方式を想定しています。
 * </p>
 */
public class SocketCInvoker implements CInvoker {

    /** 接続先ホスト */
    private final String host;

    /** 接続先ポート */
    private final int port;

    public SocketCInvoker(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 常駐 C プロセスへソケット接続して処理を依頼します。
     *
     * @param request C へ渡す入力
     * @return C 側の結果
     * @throws Exception 通信失敗時
     */
    @Override
    public CResult execute(CRequest request) throws Exception {
        Socket socket = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            socket = new Socket(host, port);

            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));

            // C サーバへ JSON を1行送る
            writer.write(JsonUtil.writeValueAsString(request));
            writer.newLine();
            writer.flush();

            // 応答も1行で受け取る
            String responseJson = reader.readLine();
            if (responseJson == null || responseJson.trim().length() == 0) {
                return CResult.failure("C response is empty");
            }

            return JsonUtil.readValue(responseJson, CResult.class);

        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    // 必要ならログ出力
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // 必要ならログ出力
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // 必要ならログ出力
                }
            }
        }
    }
}