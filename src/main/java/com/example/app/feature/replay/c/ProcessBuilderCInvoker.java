package com.example.app.feature.replay.c;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import com.example.app.common.json.JsonUtil;

/**
 * ProcessBuilder を使用して C 実行ファイルを起動する実装です。
 *
 * <p>
 * Java から外部プロセスとして C 実行ファイルを起動し、
 * 標準入力へ JSON を渡し、標準出力から JSON を受け取ります。
 * </p>
 */
public class ProcessBuilderCInvoker implements CInvoker {

    /** 起動する C 実行ファイルのパス */
    private final String commandPath;

    /** C 実行待ちタイムアウト（ミリ秒） */
    private final long timeoutMillis;

    public ProcessBuilderCInvoker(String commandPath, long timeoutMillis) {
        this.commandPath = commandPath;
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * C 実行ファイルを起動して処理を実行します。
     *
     * @param request C へ渡す入力
     * @return C 側の結果
     * @throws Exception 実行失敗時
     */
    @Override
    public CResult execute(CRequest request) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(commandPath);

        // 標準エラーを標準出力へマージする
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), Charset.forName("UTF-8")));
            reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));

            // C へ JSON リクエストを送信
            writer.write(JsonUtil.writeValueAsString(request));
            writer.flush();
            writer.close();

            // C からの応答を全文読み取る
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            // タイムアウト付きでプロセス終了待ち
            boolean finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return CResult.failure("C process timeout");
            }

            // 終了コード異常時は失敗扱い
            if (process.exitValue() != 0) {
                return CResult.failure("C process exit code=" + process.exitValue());
            }

            String responseJson = responseBuilder.toString();
            if (responseJson == null || responseJson.trim().length() == 0) {
                return CResult.failure("C response is empty");
            }

            // 受信JSONを CResult に変換
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
        }
    }
}