package nablarch.fw.web;

import java.util.Arrays;
import java.util.Collection;

/**
 * {@link RestMockHttpRequest}のビルダークラス。
 */
public class RestMockHttpRequestBuilder {

    /** 利用可能な{@link BodyConverter} */
    private Collection<? extends BodyConverter> bodyConverters = Arrays.asList(
            new StringBodyConverter()
            , new JacksonBodyConverter()
    );
    /** デフォルトContent-Type */
    private String defaultContentType = "application/json";

    /**
     * 引数で渡されたメソッド、URIで{@link RestMockHttpRequest}を生成する。
     *
     * @param httpMethod HTTPメソッド
     * @param uri        URI
     * @return 生成された{@link RestMockHttpRequest}
     */
    private RestMockHttpRequest newRequest(String httpMethod, String uri) {
        return new RestMockHttpRequest(bodyConverters, defaultContentType).setMethod(httpMethod)
                .setRequestUri(uri);
    }

    /**
     * GETのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest get(String uri) {
        return newRequest("GET", uri);
    }

    /**
     * POSTのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest post(String uri) {
        return newRequest("POST", uri);
    }

    /**
     * PUTのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest put(String uri) {
        return newRequest("PUT", uri);
    }

    /**
     * DELETEのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest delete(String uri) {
        return newRequest("DELETE", uri);
    }

    /**
     * デフォルトContent-Typeを設定する。
     *
     * @param defaultContentType Content-Type
     */
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    /**
     * 利用可能な{@link BodyConverter}を設定する。
     *
     * @param bodyConverters {@link BodyConverter}
     */
    public void setBodyConverters(Collection<? extends BodyConverter> bodyConverters) {
        this.bodyConverters = bodyConverters;
    }
}
