package com.example.app.support.debug.guard;

/**
 * support.debug 有効可否の最小判定クラスです。
 * 必要に応じて properties 読み込みへ差し替えてください。
 */
public class DebugEnabledChecker {

    public boolean isEnabled() {
        String value = System.getProperty("app.support.debug.enabled");
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv("APP_SUPPORT_DEBUG_ENABLED");
        }
        return "true".equalsIgnoreCase(value);
    }
}
