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
}
