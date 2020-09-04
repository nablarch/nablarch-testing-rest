package nablarch.test.core.http;

import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * {@link HttpRequest}と{@link HttpResponse}に追加処理を実行するインターフェース
 */
public interface RequestResponseProcessor {

    /**
     * リクエストに追加処理を実行する
     *
     * @param request HTTPリクエスト
     * @return 追加処理を施したHTTPリクエスト
     */
    HttpRequest processRequest(HttpRequest request);

    /**
     * レスポンスに追加処理を実行する
     *
     * @param request HTTPレスポンス
     * @return 追加処理を施したHTTPレスポンス
     */
    HttpResponse processResponse(HttpRequest request, HttpResponse response);

    /**
     * 内部状態をリセットする
     * テストケースをまたいで内部状態が引き継がれないために呼び出すメソッド。
     * 内部状態を持たない場合や、複数テストケース間で状態を引き継いでも問題ない場合は何もしない。
     */
    void reset();
}
