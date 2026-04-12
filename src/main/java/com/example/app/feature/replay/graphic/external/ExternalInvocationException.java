package com.example.app.feature.replay.graphic.external;

/**
 * C プロセス呼び出し失敗時の例外です。
 */
public class ExternalInvocationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExternalInvocationException(String message) {
        super(message);
    }

    public ExternalInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
