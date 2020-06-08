package nablarch.fw.web;

import nablarch.core.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * RESTfulウェブサービステスト用の{@link HttpRequest}モッククラス。
 */
public class RestMockHttpRequest extends MockHttpRequest {

    /** 改行文字 */
    private static final String LS = "\r\n";

    /** bodyを書き出すために利用可能な{@link HttpBodyWriter} */
    private final Collection<? extends HttpBodyWriter> httpBodyWriters;
    /** デフォルトContent-Type */
    private final String defaultContentType;
    /** リクエストボディ */
    private Object body;

    /**
     * 引数で渡された{@link HttpBodyWriter}の{@link Collection}とデフォルトContent-Typeを持つオブジェクトを生成する。
     *
     * @param httpBodyWriters    利用可能な{@link HttpBodyWriter}
     * @param defaultContentType デフォルトContent-Type
     */
    public RestMockHttpRequest(Collection<? extends HttpBodyWriter> httpBodyWriters,
                               String defaultContentType) {
        this.httpBodyWriters = httpBodyWriters;
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
        if (getContentType() == null && defaultContentType != null) {
            setContentType(defaultContentType);
        }
        return this;
    }

    /**
     * Content-Typeヘッダーを取得する。
     *
     * @return Content-Type
     */
    public ContentType getContentType() {
        String rawContentType = getHeader("Content-Type");
        if (StringUtil.hasValue(rawContentType)) {
            return new ContentType(rawContentType);
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
    public String toString() {
        Map<String, String[]> paramMap = getParamMap();

        if (body != null && !paramMap.isEmpty()) {
            throw new IllegalStateException("set only one of paramMap or body.");
        }

        String encodedUri = urlEncode(getRequestUri());
        String encodedParams = encodeParams(paramMap);
        String bodyStr = convertBody();

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

        int contentLength = bodyStr.getBytes().length;
        if (contentLength > 0) {
            getHeaderMap().put("Content-Length", String.valueOf(contentLength));
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append(getMethod())
                .append(' ')
                .append(encodedUri)
                .append(' ')
                .append(getHttpVersion())
                .append(LS);


        Map<String, String> headers = this.getHeaderMap();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            buffer.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue());
            buffer.append(LS);
        }

        buffer.append(LS);
        buffer.append(bodyStr);
        return buffer.toString();
    }

    /**
     * リクエストパラメータのMapをURLエンコードし結合する。
     *
     * @param paramMap リクエストパラメータ
     * @return URLエンコードされ結合されたリクエストパラメータ
     */
    private String encodeParams(Map<String, String[]> paramMap) {
        if (paramMap.isEmpty()) {
            return "";
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
        int index = uri.indexOf("?");
        if (index == -1) {
            return uri;
        }
        String requestPath = uri.substring(0, index);

        String queryString = uri.substring(index + 1);
        String[] params = queryString.split("&");
        StringBuilder sb = new StringBuilder();
        for (String param : params) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(getEncodedParam(param));
        }

        return requestPath + "?" + sb.toString();
    }

    /**
     * "name=value"形式のリクエストパラメータをURLエンコードする。
     *
     * @param paramString エンコード対象のパラメータ
     * @return URLエンコードされたパラメータ
     */
    private String getEncodedParam(String paramString) {
        String[] param = paramString.split("=");
        if (param.length != 2) {
            throw new IllegalArgumentException(paramString + " must be name=value format.");
        }
        return param[0] + "=" + encode(param[1]);
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
        ContentType contentType = getContentType();
        if (contentType != null) {
            HttpBodyWriter detectedHttpBodyWriter = findHttpBodyWriter(contentType);
            return detectedHttpBodyWriter.writeValueAsString(body, contentType);
        } else if (body != null) {
            throw new RuntimeException("there was no Content-Type header but body was not empty.");
        } else {
            return "";
        }
    }

    /**
     * Content-Typeに合った{@link HttpBodyWriter}を見つける。
     *
     * @return 見つかった{@link HttpBodyWriter}
     */
    private HttpBodyWriter findHttpBodyWriter(ContentType contentType) {
        for (HttpBodyWriter httpBodyWriter : httpBodyWriters) {
            if (httpBodyWriter.isWritable(body, contentType)) {
                return httpBodyWriter;
            }
        }
        throw new RuntimeException("unsupported media type requested. Content-Type = [ " + contentType + " ]");
    }
}
