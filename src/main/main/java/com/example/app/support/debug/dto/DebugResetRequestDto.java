package com.example.app.support.debug.dto;

import java.io.Serializable;

public class DebugResetRequestDto implements Serializable {

    private String resetType;

    public String getResetType() {
        return resetType;
    }

    public void setResetType(String resetType) {
        this.resetType = resetType;
    }
}
