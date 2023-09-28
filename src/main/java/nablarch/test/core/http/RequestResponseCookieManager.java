package nablarch.test.core.http;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.web.HttpCookie;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.RestMockHttpRequest;

import java.util.List;

/**
 * Cookieを引き継ぐためのプロセッサ。
 * レスポンス内の{@link HttpCookie}より指定されたCookieの値を取得し、
 * リクエストのCookieとして付加する。
 */
public class RequestResponseCookieManager implements RequestResponseProcessor {
    private static final Logger LOGGER = LoggerManager.get(RequestResponseCookieManager.class);

    private String cookieValue;

    private String cookieName;

    @Override
    public HttpRequest processRequest(HttpRequest request) {
        if (request instanceof RestMockHttpRequest) {
            RestMockHttpRequest restMockHttpRequest = (RestMockHttpRequest) request;
            if (cookieValue != null) {
                HttpCookie cookie = restMockHttpRequest.getCookie();
                cookie.put(cookieName, cookieValue);
                restMockHttpRequest.setCookie(cookie);
                logDebug("Set cookie: " + cookieName + " = " + cookieValue);
            }
        }

        return request;
    }

    @Override
    public HttpResponse processResponse(HttpRequest request, HttpResponse response) {
        if (cookieName == null) {
            throw new IllegalStateException("cookieName must be set.");
        }

        List<HttpCookie> cookies = response.getHttpCookies();

        for (HttpCookie cookie : cookies) {
            if (cookie.containsKey(cookieName)) {
                cookieValue = cookie.get(cookieName);
                logDebug("Get cookie: " + cookieName + " = " + cookieValue);
                return response;
            }
        }

        logDebug("Set-Cookie header value does not contain " + cookieName + ".");
        return response;
    }

    @Override
    public void reset() {
        cookieValue = null;
    }

    /**
     * Cookieの名前を設定する。
     *
     * @param cookieName Cookie名
     */
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    private void logDebug(String message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug(message);
        }
    }
}
