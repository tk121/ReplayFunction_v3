package com.example.app.feature.replay.graphic.c;

/**
 * C プロセス呼び出し失敗時の例外です。
 */
public class CInvocationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CInvocationException(String message) {
        super(message);
    }

    public CInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
