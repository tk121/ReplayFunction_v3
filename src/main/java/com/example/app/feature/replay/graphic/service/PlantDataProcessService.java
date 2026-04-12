package com.example.app.feature.replay.graphic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.app.feature.replay.graphic.entity.PlantDataLog;
import com.example.app.feature.replay.graphic.external.ExternalInvoker;
import com.example.app.feature.replay.graphic.external.plant.PlantAcceptedResponse;
import com.example.app.feature.replay.graphic.external.plant.PlantAsyncRequest;
import com.example.app.feature.replay.graphic.external.plant.PlantAsyncRequestItem;

/**
 * plant_data_log を非同期 C サーバへ送るサービスです。
 */
public class PlantDataProcessService {

    private final ExternalInvoker<PlantAsyncRequest, PlantAcceptedResponse> plantCInvoker;

    public PlantDataProcessService(
            ExternalInvoker<PlantAsyncRequest, PlantAcceptedResponse> plantCInvoker) {
        this.plantCInvoker = plantCInvoker;
    }

    /**
     * Plant データ一覧を C サーバへ送信します。
     *
     * @param plantDataList plant_data_log 取得結果
     * @return ACCEPTED 応答
     * @throws Exception 送信失敗時
     */
    public PlantAcceptedResponse submit(List<PlantDataLog> plantDataList) throws Exception {
        PlantAsyncRequest request = toRequest(plantDataList);
        PlantAcceptedResponse response = plantCInvoker.execute(request);

        if (response == null) {
            throw new IllegalStateException("Plant C response is null");
        }

        if (!response.isAccepted()) {
            throw new IllegalStateException(
                    "Plant C request was not accepted. status=" + response.getStatus()
                    + ", code=" + response.getCode()
                    + ", message=" + response.getMessage());
        }

        return response;
    }

    private PlantAsyncRequest toRequest(List<PlantDataLog> plantDataList) {
        PlantAsyncRequest request = new PlantAsyncRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setThreadId(Thread.currentThread().getId());
        request.setRequestType("PLANT_AI_CALC");
        request.setItems(toItems(plantDataList));
        return request;
    }

    private List<PlantAsyncRequestItem> toItems(List<PlantDataLog> plantDataList) {
        List<PlantAsyncRequestItem> items = new ArrayList<PlantAsyncRequestItem>();

        if (plantDataList == null) {
            return items;
        }

        for (PlantDataLog log : plantDataList) {
            PlantAsyncRequestItem item =
                    new PlantAsyncRequestItem(log.getSymbol(), log.getAiValue());
            items.add(item);
        }

        return items;
    }
}