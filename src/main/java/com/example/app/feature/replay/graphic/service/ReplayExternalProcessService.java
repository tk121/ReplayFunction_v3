package com.example.app.feature.replay.graphic.service;

import java.util.List;

import com.example.app.feature.replay.graphic.entity.PlantDataLog;
import com.example.app.feature.replay.graphic.external.plant.PlantAcceptedResponse;

/**
 * Replay から利用する外部プロセス連携の窓口です。
 *
 * <p>
 * 今は Plant 用 C プロセス連携のみを持ちますが、
 * 将来 Replay 用 / Alert 用などが増えても、
 * ReplayCoordinator の依存を増やさずにここへ集約できます。
 * </p>
 */
public class ReplayExternalProcessService {

    private final PlantDataProcessService plantDataProcessService;

    public ReplayExternalProcessService(PlantDataProcessService plantDataProcessService) {
        this.plantDataProcessService = plantDataProcessService;
    }

    /**
     * Plant データを非同期 C サーバへ送信します。
     *
     * @param plantDataList plant_data_log 取得結果
     * @return C サーバの ACCEPTED 応答
     * @throws Exception 呼び出し失敗時
     */
    public PlantAcceptedResponse submitPlantData(List<PlantDataLog> plantDataList) throws Exception {
        return plantDataProcessService.submit(plantDataList);
    }
}