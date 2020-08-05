package nablarch.test.core.http;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.RestMockHttpRequest;

/**
 * セッションストアを引き継ぐためのプロセッサ。
 * セッションIDをレスポンスの"Set-Cookie"ヘッダーから抽出し
 * リクエストのCookieとして付加する。
 */
public class NablarchSIDManager implements RequestResponseProcessor {
    private static final Logger LOGGER = LoggerManager.get(NablarchSIDManager.class);
    private String nablarchSID;
    private String cookieName = "NABLARCH_SID";

    @Override
    public HttpRequest processRequest(HttpRequest request) {
        if (request instanceof RestMockHttpRequest) {
            RestMockHttpRequest restMockHttpRequest = (RestMockHttpRequest) request;
            if (nablarchSID != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.logDebug("Set session ID: " + cookieName + " = " + nablarchSID);
                }
                restMockHttpRequest.setHeader("Cookie", cookieName + "=" + nablarchSID);
            }
        }
        return request;
    }

    @Override
    public HttpResponse processResponse(HttpRequest request, HttpResponse response) {
        String header = response.getHeader("Set-Cookie");
        if (header != null && !header.isEmpty()) {
            String value = header.split(";")[0];
            if (value.startsWith(cookieName + "=")) {
                nablarchSID = value.substring((cookieName + "=").length());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.logDebug("Get session ID: " + cookieName + " = " + nablarchSID);
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.logDebug("Set-Cookie header value does not contain " + cookieName
                            + ". header value = " + header);
                }
            }
        }
        return response;
    }

    /**
     * セッションIDを特定するCookieの名前を設定する。
     *
     * @param cookieName セッションIDのCookie名
     */
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }
}
