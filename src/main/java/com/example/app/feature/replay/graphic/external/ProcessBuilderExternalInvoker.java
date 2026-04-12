package com.example.app.feature.replay.graphic.external;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import com.example.app.common.json.JsonUtil;

/**
 * ProcessBuilder で外部プロセスを起動する実装です。
 *
 * @param <REQ> 送信リクエスト型
 * @param <RES> 受信レスポンス型
 */
public class ProcessBuilderExternalInvoker<REQ, RES> implements ExternalInvoker<REQ, RES> {

    private final String commandPath;
    private final long timeoutMillis;
    private final Class<RES> responseClass;

    public ProcessBuilderExternalInvoker(String commandPath, long timeoutMillis, Class<RES> responseClass) {
        this.commandPath = commandPath;
        this.timeoutMillis = timeoutMillis;
        this.responseClass = responseClass;
    }

    @Override
    public RES execute(REQ request) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(commandPath);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), Charset.forName("UTF-8")));
            reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));

            writer.write(JsonUtil.writeValueAsString(request));
            writer.flush();
            writer.close();

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            boolean finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("C process timeout");
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException("C process exit code=" + process.exitValue());
            }

            String responseJson = responseBuilder.toString();
            if (responseJson.trim().length() == 0) {
                throw new RuntimeException("C response is empty");
            }

            return JsonUtil.readValue(responseJson, responseClass);

        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception e) {}
            }
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
        }
    }
}