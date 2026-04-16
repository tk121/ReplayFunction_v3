package com.example.app.support.debug.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.replay.control.service.ReplayCommandService;
import com.example.app.support.debug.dto.DebugCommandRequestDto;
import com.example.app.support.debug.dto.DebugCommandResultDto;
import com.example.app.support.debug.model.DebugCommandType;

/**
 * デバッグ用コマンド実行サービスです。
 */
public class DebugCommandService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DebugCommandResultDto execute(DebugCommandRequestDto request) {
        DebugCommandResultDto result = new DebugCommandResultDto();

        try {
            ReplayCommandService replayCommandService = AppRuntime.getReplayCommandService();
            if (replayCommandService == null) {
                throw new IllegalStateException("ReplayCommandService is null.");
            }

            DebugCommandType type = DebugCommandType.valueOf(request.getCommandType());

            switch (type) {
                case PLAY:
                    replayCommandService.play();
                    break;
                case STOP:
                    replayCommandService.stop();
                    break;
                case GO_HEAD:
                    replayCommandService.goHead();
                    break;
                case GO_TAIL:
                    replayCommandService.goTail();
                    break;
                case SET_SPEED:
                    replayCommandService.setSpeed(parseInt(request.getSpeed(), "speed"));
                    break;
                case JUMP_TIME:
                    replayCommandService.jumpTime(parseDateTime(request.getReplayTime(), "replayTime"));
                    break;
                case APPLY_CONDITION:
                    replayCommandService.applyCondition(
                            parseDateTime(request.getTargetFrom(), "targetFrom"),
                            parseDateTime(request.getTargetTo(), "targetTo"));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported commandType: " + type);
            }

            result.setSuccess(true);
            result.setMessage("debug command executed. commandType=" + type.name());

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return result;
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " is invalid. value=" + value);
        }
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        try {
            return LocalDateTime.parse(value, FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    fieldName + " is invalid. expected format=yyyy-MM-dd HH:mm:ss, value=" + value);
        }
    }
}
