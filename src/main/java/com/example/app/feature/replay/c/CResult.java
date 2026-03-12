package com.example.app.feature.replay.c;

/**
 * C プロセスから返される結果です。
 *
 * <p>
 * 現時点では、成功 / 失敗 とメッセージのみを保持します。
 * Java 側は C の内部状態までは保持せず、
 * 「イベント適用に成功したかどうか」だけを管理します。
 * </p>
 */
public class CResult {

    /** 成功時 true、失敗時 false */
    private boolean success;

    /** 補足メッセージ */
    private String message;

    /**
     * 成功結果を生成します。
     *
     * @return success=true の CResult
     */
    public static CResult success() {
        CResult result = new CResult();
        result.setSuccess(true);
        return result;
    }

    /**
     * 失敗結果を生成します。
     *
     * @param message 失敗メッセージ
     * @return success=false の CResult
     */
    public static CResult failure(String message) {
        CResult result = new CResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}