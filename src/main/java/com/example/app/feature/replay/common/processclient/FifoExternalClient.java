package com.example.app.feature.replay.common.processclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.example.app.common.json.JsonUtil;

public class FifoExternalClient<REQ, RES> implements ExternalClient<REQ, RES> {

    private final String requestFifoPath;
    private final String responseFifoPath;
    private final Class<RES> responseType;

    public FifoExternalClient(
            String requestFifoPath,
            String responseFifoPath,
            Class<RES> responseType) {
        this.requestFifoPath = requestFifoPath;
        this.responseFifoPath = responseFifoPath;
        this.responseType = responseType;
    }

    @Override
    public RES send(REQ request) throws Exception {
        String requestJson = JsonUtil.writeValue(request);

        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(requestFifoPath), "UTF-8"));
            writer.write(requestJson);
            writer.newLine();
            writer.flush();

            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(responseFifoPath), "UTF-8"));
            String responseJson = reader.readLine();

            if (responseJson == null) {
                throw new IllegalStateException("fifo response is null. responseFifo=" + responseFifoPath);
            }

            return JsonUtil.readValue(responseJson, responseType);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}