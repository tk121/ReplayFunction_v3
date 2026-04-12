package com.example.app.feature.replay.graphic.external;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.example.app.common.json.JsonUtil;

/**
 * length-prefixed JSON プロトコルで常駐 C プロセスへ依頼する実装です。
 *
 * <p>
 * Protocol:
 * [4byte length (big-endian)][UTF-8 JSON bytes]
 * </p>
 *
 * <p>
 * C 側が epoll + TCP サーバで待ち受ける方式に対応します。
 * </p>
 *
 * @param <REQ> 送信リクエスト型
 * @param <RES> 受信レスポンス型
 */
public class LengthPrefixedSocketExternalInvoker<REQ, RES> implements ExternalInvoker<REQ, RES> {

    /** 接続先ホスト */
    private final String host;

    /** 接続先ポート */
    private final int port;

    /** 接続タイムアウト（ミリ秒） */
    private final int connectTimeoutMillis;

    /** 読み取りタイムアウト（ミリ秒） */
    private final int readTimeoutMillis;

    /** レスポンス型 */
    private final Class<RES> responseClass;

    public LengthPrefixedSocketExternalInvoker(String host,
                                        int port,
                                        int connectTimeoutMillis,
                                        int readTimeoutMillis,
                                        Class<RES> responseClass) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.responseClass = responseClass;
    }

    @Override
    public RES execute(REQ request) throws Exception {
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMillis);
            socket.setSoTimeout(readTimeoutMillis);

            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

            // request -> JSON UTF-8 bytes
            String requestJson = JsonUtil.writeValueAsString(request);
            byte[] requestBytes = requestJson.getBytes(StandardCharsets.UTF_8);

            // [4byte length][json]
            dos.writeInt(requestBytes.length);
            dos.write(requestBytes);
            dos.flush();

            // response [4byte length][json]
            int responseLength = dis.readInt();
            if (responseLength <= 0) {
                throw new ExternalInvocationException("C response length is invalid: " + responseLength);
            }

            byte[] responseBytes = new byte[responseLength];
            dis.readFully(responseBytes);

            String responseJson = new String(responseBytes, StandardCharsets.UTF_8);
            if (responseJson.trim().length() == 0) {
                throw new ExternalInvocationException("C response is empty");
            }

            return JsonUtil.readValue(responseJson, responseClass);

        } catch (Exception e) {
            throw new ExternalInvocationException(
                    "Failed to invoke C socket. host=" + host + ", port=" + port, e);
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception e) {
                    // 必要ならログ
                }
            }
            if (dos != null) {
                try {
                    dos.close();
                } catch (Exception e) {
                    // 必要ならログ
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // 必要ならログ
                }
            }
        }
    }
}
