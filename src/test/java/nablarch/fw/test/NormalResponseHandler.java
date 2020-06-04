package nablarch.fw.test;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;

/**
 * ユニットテスト用ハンドラ。
 * 常にHTTPステータス 200 OK を返す。
 */
public class NormalResponseHandler implements HttpRequestHandler {
    @Override
    public HttpResponse handle(HttpRequest httpRequest, ExecutionContext executionContext) {
        return new HttpResponse(200);
    }
}
