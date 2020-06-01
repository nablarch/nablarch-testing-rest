package nablarch.fw.web;

import nablarch.core.util.StringUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public String getContentType() {
        return getHeader("Content-Type");
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
        String bodyStr = convertBody();
        String encodedUri = urlEncode(getRequestUri());

        Map<String, String[]> paramMap = getParamMap();
        String encodedParams = encodeParams(paramMap);
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
        getHeaderMap().put("Content-Length", String.valueOf(bodyStr.getBytes().length));

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
            String[] values = param.getValue();
            for (int i = 0; i < values.length; i++) {
                buffer.append(name)
                        .append("=")
                        .append(encode(values[i]));
                if (i < values.length - 1) {
                    buffer.append("&");
                }
            }
            if (params.hasNext()) {
                buffer.append("&");
            }
        }
        return buffer.toString();
    }

    /** クエリストリングを持つURLの書式 */
    public static final Pattern URI_PATTERN = Pattern.compile("(.*)\\?(.*)");

    /**
     * リクエストURIをURLエンコードする。
     *
     * @param uri リクエストURI
     * @return URLエンコードされたリクエストURI
     */
    private String urlEncode(String uri) {
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (matcher.find()) {
            String queryString = matcher.group(2);
            String[] params = queryString.split("&");

            StringBuilder sb = new StringBuilder();
            for (String param : params) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(getEncodedParam(param));
            }

            return matcher.group(1) + "?" + sb.toString();
        } else {
            return uri;
        }
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
        StringWriter bodyWriter = new StringWriter();
        if (getContentType() != null) {
            writeBody(bodyWriter);
        } else if (body != null) {
            throw new RuntimeException("there was no Content-Type header but body was not empty.");
        }
        bodyWriter.flush();
        try {
            bodyWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("an error occurred while Writer was being closed.", e);
        }
        return bodyWriter.toString();
    }

    /**
     * {@link StringWriter}にボディを書き込む。
     *
     * @param bodyWriter 書き込み対象
     */
    private void writeBody(StringWriter bodyWriter) {
        HttpBodyWriter detectedHttpBodyWriter = findHttpBodyWriter();
        try {
            detectedHttpBodyWriter.write(body, getContentType(), bodyWriter);
        } catch (IOException e) {
            throw new RuntimeException("an error occurred while Writer was writing body.", e);
        }
    }

    /**
     * Content-Typeに合った{@link HttpBodyWriter}を見つける。
     *
     * @return 見つかった{@link HttpBodyWriter}
     */
    private HttpBodyWriter findHttpBodyWriter() {
        for (HttpBodyWriter httpBodyWriter : httpBodyWriters) {
            if (httpBodyWriter.isWritable(body, getContentType())) {
                return httpBodyWriter;
            }
        }
        throw new RuntimeException("unsupported media type requested. Content-Type = [ " + getContentType() + " ]");
    }
}
