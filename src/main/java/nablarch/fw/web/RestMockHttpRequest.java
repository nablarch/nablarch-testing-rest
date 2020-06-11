package nablarch.fw.web;

import nablarch.core.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * RESTfulウェブサービステスト用の{@link HttpRequest}モッククラス。
 */
public class RestMockHttpRequest extends MockHttpRequest {

    /** 改行文字 */
    private static final String LS = "\r\n";

    /** bodyを書き出すために利用可能な{@link BodyConverter} */
    private final Collection<? extends BodyConverter> bodyConverters;
    /** デフォルトContent-Type */
    private final String defaultContentType;
    /** リクエストボディ */
    private Object body;
    public static final String CONTENT_LENGTH_KEY = "Content-Length";

    /**
     * 引数で渡された{@link BodyConverter}の{@link Collection}とデフォルトContent-Typeを持つオブジェクトを生成する。
     *
     * @param bodyConverters     利用可能な{@link BodyConverter}
     * @param defaultContentType デフォルトContent-Type
     */
    public RestMockHttpRequest(Collection<? extends BodyConverter> bodyConverters,
                               String defaultContentType) {
        this.bodyConverters = bodyConverters;
        this.defaultContentType = defaultContentType;
    }

    /**
     * ボディを取得する。
     *
     * @return リクエストボディ
     */
    public Object getBody() {
        return body;
    }

    /**
     * リクエストボディを設定する。
     *
     * @param body リクエストボディに設定するオブジェクト
     * @return {@link RestMockHttpRequest}自身
     */
    public RestMockHttpRequest setBody(Object body) {
        this.body = body;
        if (getMediaType() == null && defaultContentType != null) {
            setContentType(defaultContentType);
        }
        return this;
    }

    /**
     * Content-TypeヘッダーからMIMEタイプを取得する。
     *
     * @return MIMEタイプ
     */
    private MediaType getMediaType() {
        String rawContentType = getHeader("Content-Type");
        if (StringUtil.hasValue(rawContentType)) {
            return new MediaType(rawContentType);
        }
        return null;
    }

    /**
     * Content-Typeを設定する。
     *
     * @param contentType Content-Typeに設定する値
     * @return {@link RestMockHttpRequest}自身
     */
    public RestMockHttpRequest setContentType(String contentType) {
        getHeaderMap().put("Content-Type", contentType);
        return this;
    }

    @Override
    public RestMockHttpRequest setMethod(String method) {
        return (RestMockHttpRequest) super.setMethod(method);
    }

    @Override
    public RestMockHttpRequest setHeaderMap(Map<String, String> headers) {
        return (RestMockHttpRequest) super.setHeaderMap(headers);
    }

    @Override
    public RestMockHttpRequest setRequestUri(String requestPath) {
        return (RestMockHttpRequest) super.setRequestUri(requestPath);
    }

    @Override
    public RestMockHttpRequest setParam(String name, String... params) {
        return (RestMockHttpRequest) super.setParam(name, params);
    }

    @Override
    public RestMockHttpRequest setParamMap(Map<String, String[]> params) {
        return (RestMockHttpRequest) super.setParamMap(params);
    }

    @Override
    public RestMockHttpRequest setCookie(HttpCookie cookie) {
        return (RestMockHttpRequest) super.setCookie(cookie);
    }

    @Override
    public RestMockHttpRequest setHttpVersion(String httpVersion) {
        return (RestMockHttpRequest) super.setHttpVersion(httpVersion);
    }

    @Override
    public RestMockHttpRequest setHost(String host) {
        return (RestMockHttpRequest) super.setHost(host);
    }

    @Override
    public RestMockHttpRequest setRequestPath(String requestPath) {
        return (RestMockHttpRequest) super.setRequestPath(requestPath);
    }

    @Override
    public String toString() {
        Map<String, String[]> paramMap = getParamMap();

        if (body != null && !paramMap.isEmpty()) {
            throw new IllegalStateException("set only one of paramMap or body.");
        }

        String encodedUri = urlEncode(getRequestUri());
        String encodedParams = encodeParams(paramMap);
        String bodyStr = convertBody();
        Map<String, String> headers = new HashMap<String, String>(this.getHeaderMap());

        if (StringUtil.hasValue(encodedParams)) {
            if ("GET".equals(getMethod())) {
                if (encodedUri.contains("?")) {
                    encodedUri = encodedUri + "&" + encodedParams;
                } else {
                    encodedUri = encodedUri + "?" + encodedParams;
                }
            } else {
                bodyStr = encodedParams;
            }
        }

        if (StringUtil.hasValue(bodyStr)) {
            setContentLength(headers, bodyStr);
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append(getMethod())
                .append(' ')
                .append(encodedUri)
                .append(' ')
                .append(getHttpVersion())
                .append(LS);


        for (Map.Entry<String, String> entry : headers.entrySet()) {
            buffer.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue());
            buffer.append(LS);
        }

        buffer.append(LS);
        if (StringUtil.hasValue(bodyStr)) {
            buffer.append(bodyStr);
        }
        return buffer.toString();
    }

    /**
     * 引数で渡されたヘッダーMapに Content-Length をセットする。
     * 誤った Content-Length が設定されていた場合は例外を送出する。
     *
     * @param headers ヘッダー
     * @param bodyStr リクエストボディ
     */
    private void setContentLength(Map<String, String> headers, String bodyStr) {
        int contentLength = bodyStr.getBytes().length;
        if (headers.containsKey(CONTENT_LENGTH_KEY)) {
            String contentLengthOrg = headers.get(CONTENT_LENGTH_KEY);
            if (Integer.parseInt(contentLengthOrg) != contentLength) {
                throw new RuntimeException("wrong Content-Length[" + contentLengthOrg + "] was set."
                        + "correct length is [" + contentLength + "].");
            }
        }
        if (contentLength > 0) {
            headers.put(CONTENT_LENGTH_KEY, String.valueOf(contentLength));
        }
    }

    /**
     * リクエストパラメータのMapをURLエンコードし結合する。
     *
     * @param paramMap リクエストパラメータ
     * @return URLエンコードされ結合されたリクエストパラメータ
     */
    private String encodeParams(Map<String, String[]> paramMap) {
        if (paramMap.isEmpty()) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        Iterator<Map.Entry<String, String[]>> params = paramMap.entrySet().iterator();
        while (params.hasNext()) {
            Map.Entry<String, String[]> param = params.next();
            String name = param.getKey();
            Iterator<String> values = Arrays.asList(param.getValue()).iterator();
            while (values.hasNext()) {
                buffer.append(name)
                        .append("=")
                        .append(encode(values.next()));
                if (values.hasNext()) {
                    buffer.append("&");
                }
            }
            if (params.hasNext()) {
                buffer.append("&");
            }
        }
        return buffer.toString();
    }

    /**
     * リクエストURIをURLエンコードする。
     *
     * @param uri リクエストURI
     * @return URLエンコードされたリクエストURI
     */
    private String urlEncode(String uri) {
        try {
            return new URI(uri).toASCIIString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("url encoding failed. cause[" + e.getMessage() + "]", e);
        }
    }

    /**
     * UTF-8でURLエンコードする。
     *
     * @param value エンコード対象
     * @return URLエンコードされた文字列
     */
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("url encoding failed.", e);
        }
    }

    /**
     * リクエストボディを{@link String}に変換する。
     *
     * @return リクエストボディの文字列
     */
    private String convertBody() {
        if (body == null) {
            return null;
        }
        MediaType mediaType = getMediaType();
        if (mediaType != null) {
            BodyConverter detectedBodyConverter = findBodyConverter(mediaType);
            return detectedBodyConverter.convert(body, mediaType);
        } else {
            throw new RuntimeException("there was no Content-Type header but body was not empty.");
        }
    }

    /**
     * MIMEタイプに合った{@link BodyConverter}を見つける。
     *
     * @return 見つかった{@link HttpBodyWriter}
     */
    private BodyConverter findBodyConverter(MediaType mediaType) {
        for (BodyConverter bodyConverter : bodyConverters) {
            if (bodyConverter.isConvertible(body, mediaType)) {
                return bodyConverter;
            }
        }
        throw new RuntimeException("unsupported media type requested. MIME type = [ " + mediaType + " ]");
    }
}
