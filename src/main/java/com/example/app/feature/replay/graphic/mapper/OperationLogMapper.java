package com.example.app.feature.replay.graphic.mapper;

import com.example.app.feature.replay.graphic.entity.OperationLog;
import com.example.app.feature.replay.graphic.external.ExternalCommandRequest;
import com.example.app.feature.replay.graphic.model.ReplayOperationEvent;

/**
 * OperationLog を replay 用の各種オブジェクトへ変換する Mapper です。
 *
 * <p>
 * 現時点では主に C プロセスへ渡す CRequest への変換を担当します。
 * </p>
 */
public class OperationLogMapper {

	
    public ExternalCommandRequest toCRequest(ReplayOperationEvent event) {
        ExternalCommandRequest request = new ExternalCommandRequest();

        request.setEventType(event.getActionType());
        request.setPageId(event.getPageId());
        request.setControlId(event.getControlId());
        request.setSymbolId(event.getButtonId());
        request.setValue(event.getValue());

        return request;
    }
    
    public ReplayOperationEvent toReplayOperationEvent(OperationLog row) {
        ReplayOperationEvent event = new ReplayOperationEvent();

        event.setOperationId(row.getOperationId());
        event.setVduNo(row.getVduNo());
        event.setActionType(row.getActionType());
        event.setPageId(row.getPageId());
        event.setControlId(row.getControlId());
        event.setButtonId(row.getButtonId());
        event.setValue(row.getValue());
        event.setOccurredAt(row.getOccurredAt());

        return event;
    }
}