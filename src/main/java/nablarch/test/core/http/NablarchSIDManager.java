package nablarch.test.core.http;

/**
 * セッションストアを引き継ぐためのプロセッサ。
 * セッションIDをレスポンスの"Set-Cookie"ヘッダーから抽出し
 * リクエストのCookieとして付加する。
 */
public class NablarchSIDManager extends RequestResponseCookieManager {
    public NablarchSIDManager() {
        setCookieName("NABLARCH_SID");
    }
}
