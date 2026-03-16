package com.example.app.common.runtime;

import javax.sql.DataSource;

import com.example.app.feature.replay.controller.ws.WsHub;
import com.example.app.feature.replay.engine.ReplayEngine;
import com.example.app.feature.replay.service.ReplayControlConfig;
import com.example.app.feature.replay.service.ReplayCoordinator;
import com.example.app.feature.replay.service.ReplayResponseService;
import com.example.app.feature.replay.service.ReplaySessionService;

/**
 * アプリケーション起動時に生成した共有オブジェクトを保持するクラスです。
 *
 * <p>
 * Listener で生成した DataSource や Service、Engine、WebSocket ハブなどを
 * アプリ全体から参照できるようにまとめて保持します。
 * </p>
 *
 * <p>
 * 現在は replay 機能用の共有オブジェクトを保持していますが、
 * 今後 trend や event などの機能が増えた場合も、
 * 同じ考え方で機能ごとの共有オブジェクトを追加できます。
 * </p>
 *
 * <p>
 * このクラスはインスタンス化せず、静的メソッドで利用します。
 * </p>
 */
public final class AppRuntime {

    /**
     * アプリ全体で共通利用する DataSource です。
     */
    private static DataSource dataSource;

    /**
     * replay 機能用の WebSocket クライアント管理ハブです。
     *
     * <p>
     * 接続中クライアントの保持や、状態のブロードキャストに使用します。
     * </p>
     */
    private static WsHub replayWsHub;

    /**
     * replay 機能用の状態管理サービスです。
     *
     * <p>
     * room ごとの ReplayState を保持し、取得・作成・更新を担当します。
     * </p>
     */
    private static ReplaySessionService replaySessionService;

    /**
     * replay 機能用のレスポンス生成サービスです。
     *
     * <p>
     * ReplayState から HTTP / WebSocket 用 DTO を組み立てるために使用します。
     * </p>
     */
    private static ReplayResponseService replayResponseService;

    /**
     * replay 機能用の再生エンジンです。
     *
     * <p>
     * 一定周期で再生時刻を進め、event_log の再生処理を進行させます。
     * </p>
     */
    private static ReplayEngine replayEngine;

    /**
     * replay 機能用の業務制御サービスです。
     *
     * <p>
     * 再生開始・停止・早送りなどの制御や、
     * event_log のイベント適用処理を担当します。
     * </p>
     */
    private static ReplayCoordinator replayCoordinator;
    
    /** replay 用操作権制御設定 */
    private static ReplayControlConfig replayControlConfig;

    /**
     * インスタンス化を禁止するための private コンストラクタです。
     *
     * <p>
     * このクラスは静的な共有領域としてのみ使用します。
     * </p>
     */
    private AppRuntime() {
    }

    /**
     * replay 機能で利用する共有オブジェクトを初期化します。
     *
     * <p>
     * AppInitListener からアプリ起動時に 1回だけ呼び出し、
     * 生成済みのオブジェクトを保持します。
     * </p>
     *
     * <p>
     * synchronized を付けることで、初期化処理が同時に走った場合でも
     * 競合しにくくしています。
     * </p>
     *
     * @param ds DataSource
     * @param wsHub replay 用 WebSocket ハブ
     * @param sessionService replay 用状態管理サービス
     * @param responseService replay 用レスポンス生成サービス
     * @param engine replay 用再生エンジン
     * @param coordinator replay 用制御サービス
     */
    public static synchronized void initializeReplay(
            DataSource ds,
            WsHub wsHub,
            ReplaySessionService sessionService,
            ReplayResponseService responseService,
            ReplayEngine engine,
            ReplayCoordinator coordinator,
            ReplayControlConfig controlConfig) {

        // 共通 DataSource を保持
        dataSource = ds;

        // replay 機能の共有オブジェクトを保持
        replayWsHub = wsHub;
        replaySessionService = sessionService;
        replayResponseService = responseService;
        replayEngine = engine;
        replayCoordinator = coordinator;
        replayControlConfig = controlConfig;
    }

    /**
     * 共通 DataSource を返します。
     *
     * @return DataSource
     */
    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * replay 機能用 WebSocket ハブを返します。
     *
     * @return WsHub
     */
    public static WsHub getReplayWsHub() {
        return replayWsHub;
    }

    /**
     * replay 機能用状態管理サービスを返します。
     *
     * @return ReplaySessionService
     */
    public static ReplaySessionService getReplaySessionService() {
        return replaySessionService;
    }

    /**
     * replay 機能用レスポンス生成サービスを返します。
     *
     * @return ReplayResponseService
     */
    public static ReplayResponseService getReplayResponseService() {
        return replayResponseService;
    }

    /**
     * replay 機能用再生エンジンを返します。
     *
     * @return ReplayEngine
     */
    public static ReplayEngine getReplayEngine() {
        return replayEngine;
    }

    /**
     * replay 機能用制御サービスを返します。
     *
     * @return ReplayCoordinator
     */
    public static ReplayCoordinator getReplayCoordinator() {
        return replayCoordinator;
    }
    
    public static ReplayControlConfig getReplayControlConfig() {
        return replayControlConfig;
    }
}