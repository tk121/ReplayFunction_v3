package com.example.app.feature.replay.c;

/**
 * C プロセス呼び出しインターフェースです。
 *
 * <p>
 * 実際の呼び出し方式が ProcessBuilder でも Socket でも、
 * Java 側からはこのインターフェースで統一的に扱います。
 * </p>
 */
public interface CInvoker {

    /**
     * C プロセスへイベントを渡して処理を実行します。
     *
     * @param request C へ渡す入力情報
     * @return C 側の処理結果
     * @throws Exception 呼び出し失敗時
     */
    CResult execute(CRequest request) throws Exception;
}