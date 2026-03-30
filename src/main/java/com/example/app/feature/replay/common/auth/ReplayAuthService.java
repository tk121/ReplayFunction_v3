package com.example.app.feature.replay.common.auth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReplayAuthService {

    private final Map<String, Boolean> controlCapableMap;

    public ReplayAuthService() {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        map.put("operator1", Boolean.TRUE);
        map.put("operator2", Boolean.TRUE);
        map.put("guest1", Boolean.FALSE);
        map.put("guest2", Boolean.FALSE);
        this.controlCapableMap = Collections.unmodifiableMap(map);
    }

    public LoginUser login(String userName) {
        if (userName == null || userName.trim().length() == 0) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }
        String normalized = userName.trim();
        Boolean canControl = controlCapableMap.get(normalized);
        if (canControl == null) {
            canControl = Boolean.FALSE;
        }
        return new LoginUser(normalized, normalized, canControl.booleanValue());
    }
}
