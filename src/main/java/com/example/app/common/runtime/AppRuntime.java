package com.example.app.common.runtime;

import javax.sql.DataSource;

import com.example.app.feature.auth.AuthModule;
import com.example.app.feature.replay.ReplayModule;
import com.example.app.feature.trend.TrendModule;

/**
 * アプリケーション起動時に生成した Module を保持するクラスです。
 *
 * <p>
 * 共有オブジェクトは個別の Service / Repository を直接持たず、
 * 機能単位の Module をまとめて保持します。
 * </p>
 */
public final class AppRuntime {

    private static DataSource dataSource;
    private static AuthModule authModule;
    private static ReplayModule replayModule;
    private static TrendModule trendModule;

    private AppRuntime() {
    }

    public static synchronized void initialize(
            DataSource ds,
            AuthModule authModuleInstance,
            ReplayModule replayModuleInstance,
            TrendModule trendModuleInstance) {

        dataSource = ds;
        authModule = authModuleInstance;
        replayModule = replayModuleInstance;
        trendModule = trendModuleInstance;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static AuthModule getAuthModule() {
        return authModule;
    }

    public static ReplayModule getReplayModule() {
        return replayModule;
    }

    public static TrendModule getTrendModule() {
        return trendModule;
    }
}