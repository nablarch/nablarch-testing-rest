package nablarch.test.core.http;

import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * セッションストアを引き継ぐためのプロセッサ。
 * セッションIDをレスポンスの"Set-Cookie"ヘッダーから抽出し
 * リクエストのCookieとして付加する。
 */
public class NablarchSIDManager extends RequestResponseCookieManager {

    @Override
    public HttpRequest processRequest(HttpRequest request) {
        return super.processRequest(request);
    }

    @Override
    public HttpResponse processResponse(HttpRequest request, HttpResponse response) {
        super.setCookieName("NABLARCH_SID");
        return super.processResponse(request, response);
    }
}
