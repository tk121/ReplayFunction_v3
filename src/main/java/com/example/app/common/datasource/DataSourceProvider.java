package com.example.app.common.datasource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * DataSource を取得するための共通ユーティリティクラスです。
 *
 * <p>
 * 主にアプリ起動時に JNDI へ登録された DataSource を取得するために使用します。
 * replay だけでなく、今後追加される trend や event などの機能でも
 * 共通利用できるように common 配下へ配置しています。
 * </p>
 *
 * <p>
 * このクラスはインスタンス化せず、静的メソッドで利用します。
 * </p>
 */
public final class DataSourceProvider {

    /**
     * インスタンス化を禁止するための private コンストラクタです。
     *
     * <p>
     * ユーティリティクラスなので new して使う想定はありません。
     * </p>
     */
    private DataSourceProvider() {
    }

    /**
     * 指定された JNDI 名から DataSource を取得します。
     *
     * <p>
     * Tomcat やアプリケーションサーバの context.xml / server.xml などで
     * 定義された DataSource を名前で検索して取得します。
     * </p>
     *
     * <p>
     * 例:
     * </p>
     * <pre>
     * DataSource ds = DataSourceProvider.lookup("java:comp/env/jdbc/mydb");
     * </pre>
     *
     * @param jndiName JNDI に登録された DataSource 名
     * @return 取得した DataSource
     * @throws Exception JNDI 検索失敗時
     */
    public static DataSource lookup(String jndiName) throws Exception {
        // JNDI の初期コンテキストを取得する
        Context context = new InitialContext();

        // 指定された名前で DataSource を検索して返す
        return (DataSource) context.lookup(jndiName);
    }
}