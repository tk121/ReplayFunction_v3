package com.example.app.feature.replay.common.constant;

public enum ReplayControlCommand {
	INIT,
    APPLY_CONDITION,
    PLAY,
    STOP,
    CHANGE_SPEED,
    GO_HEAD,
    GO_TAIL;

    public static ReplayControlCommand from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return APPLY_CONDITION;
        }
        return ReplayControlCommand.valueOf(value.trim());
    }

}
