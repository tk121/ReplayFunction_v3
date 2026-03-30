package com.example.app.common.json;

import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON変換を共通化するためのユーティリティクラスです。
 *
 * <p>
 * Jackson の {@link ObjectMapper} を 1つだけ保持し、
 * JSON文字列や Reader から Javaオブジェクトへ変換したり、
 * Javaオブジェクトを JSON文字列や Writer へ出力したりするために使用します。
 * </p>
 *
 * <p>
 * このクラスはインスタンス化せず、静的メソッドで利用します。
 * </p>
 */
public final class JsonUtil {

    /**
     * アプリ全体で共通利用する ObjectMapper です。
     *
     * <p>
     * ObjectMapper は生成コストがあるため、毎回 new せずに
     * 1つのインスタンスを使い回します。
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * クラス読み込み時に 1回だけ実行される初期化ブロックです。
     *
     * <p>
     * JSONに Javaクラス側で定義していない項目が含まれていても
     * エラーにせず無視する設定にしています。
     * </p>
     *
     * <p>
     * これにより、JSON側に余分な項目があっても受け取りやすくなります。
     * </p>
     */
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * インスタンス化を禁止するための private コンストラクタです。
     *
     * <p>
     * ユーティリティクラスのため、new JsonUtil() はさせません。
     * </p>
     */
    private JsonUtil() {
    }

    /**
     * 共通利用している ObjectMapper を返します。
     *
     * <p>
     * 特殊な設定や拡張処理を呼び出し元で行いたい場合に利用します。
     * ただし、共通設定を壊さないよう注意が必要です。
     * </p>
     *
     * @return 共通の ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Reader から JSON を読み込み、指定したクラスのオブジェクトに変換します。
     *
     * <p>
     * 主に Servlet の request.getReader() から
     * リクエストJSONを DTO に変換するときに使用します。
     * </p>
     *
     * @param reader JSON入力元
     * @param clazz 変換先クラス
     * @param <T> 変換先型
     * @return JSONを変換したオブジェクト
     * @throws Exception JSON解析失敗時
     */
    public static <T> T readValue(Reader reader, Class<T> clazz) throws Exception {
        return OBJECT_MAPPER.readValue(reader, clazz);
    }

    /**
     * JSON文字列を読み込み、指定したクラスのオブジェクトに変換します。
     *
     * <p>
     * WebSocket受信文字列や、Cプロセスから受け取ったJSON文字列を
     * DTOへ変換するときなどに使用します。
     * </p>
     *
     * @param json JSON文字列
     * @param clazz 変換先クラス
     * @param <T> 変換先型
     * @return JSONを変換したオブジェクト
     * @throws Exception JSON解析失敗時
     */
    public static <T> T readValue(String json, Class<T> clazz) throws Exception {
        return OBJECT_MAPPER.readValue(json, clazz);
    }

    /**
     * オブジェクトを JSON文字列へ変換します。
     *
     * <p>
     * WebSocket送信用文字列や、Cプロセスへ渡すJSON文字列を
     * 作成するときに使用します。
     * </p>
     *
     * @param value JSON化するオブジェクト
     * @return JSON文字列
     * @throws Exception JSON生成失敗時
     */
    public static String writeValueAsString(Object value) throws Exception {
        return OBJECT_MAPPER.writeValueAsString(value);
    }

    /**
     * オブジェクトを JSON として Writer へ書き出します。
     *
     * <p>
     * 主に Servlet の response.getWriter() に対して
     * レスポンスJSONを書き込むときに使用します。
     * </p>
     *
     * @param writer 出力先
     * @param value JSON化するオブジェクト
     * @throws Exception JSON生成失敗時
     */
    public static void writeValue(Writer writer, Object value) throws Exception {
        OBJECT_MAPPER.writeValue(writer, value);
    }
}