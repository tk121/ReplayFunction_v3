package com.example.app.feature.replay.graphic.c;

/**
 * C プロセス呼び出しインターフェースです。
 *
 * <p>
 * C プロセスごとに request/response 型を分けられるように、
 * ジェネリクスで表現します。
 * </p>
 *
 * @param <REQ> 送信リクエスト型
 * @param <RES> 受信レスポンス型
 */
public interface CInvoker<REQ, RES> {

    /**
     * C プロセスへ依頼を送信して応答を受け取ります。
     *
     * @param request 送信リクエスト
     * @return C 側のレスポンス
     * @throws Exception 呼び出し失敗時
     */
    RES execute(REQ request) throws Exception;
}