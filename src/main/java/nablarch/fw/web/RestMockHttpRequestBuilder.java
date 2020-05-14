package nablarch.fw.web;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.util.StringUtil;

import java.util.Collection;

/**
 * {@link RestMockHttpRequest}のビルダークラス。
 */
public class RestMockHttpRequestBuilder {

    /** 利用可能な{@link HttpBodyWriter} */
    private Collection<? extends HttpBodyWriter> httpBodyWriters;
    /** デフォルトContent-Type */
    private String defaultContentType;

    /**
     * 引数で渡されたメソッド、URIで{@link RestMockHttpRequest}を生成する。
     *
     * @param httpMethod HTTPメソッド
     * @param uri        URI
     * @return 生成された{@link RestMockHttpRequest}
     */
    private RestMockHttpRequest newRequest(String httpMethod, String uri) {
        if (httpBodyWriters == null) {
            throw new IllegalConfigurationException("httpBodyWriters has not been initialized.");
        }
        if (StringUtil.isNullOrEmpty(defaultContentType)) {
            throw new IllegalConfigurationException("defaultContentType has not been initialized.");
        }
        return new RestMockHttpRequest(httpBodyWriters, defaultContentType).setMethod(httpMethod)
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
     * GETのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest post(String uri) {
        return newRequest("POST", uri);
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
     * 利用可能な{@link HttpBodyWriter}を設定する。
     *
     * @param httpBodyWriters {@link HttpBodyWriter}
     */
    public void setHttpBodyWriters(Collection<? extends HttpBodyWriter> httpBodyWriters) {
        this.httpBodyWriters = httpBodyWriters;
    }
}
