package nablarch.fw.web;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
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
        if (body == null) {
            return super.toString();
        }
        String bodyStr = convertBody();
        getHeaderMap().put("Content-Length", String.valueOf(bodyStr.getBytes().length));

        StringBuilder buffer = new StringBuilder();
        buffer.append(getMethod());
        buffer.append(' ');
        buffer.append(getRequestUri());
        buffer.append(' ');
        buffer.append(getHttpVersion());
        buffer.append(LS);

        for (Map.Entry<String, String> header : getHeaderMap().entrySet()) {
            buffer.append(header.getKey());
            buffer.append(": ");
            buffer.append(header.getValue());
            buffer.append(LS);
        }

        buffer.append(LS);
        buffer.append(bodyStr);
        return buffer.toString();
    }

    /**
     * リクエストボディを{@link String}に変換する。
     *
     * @return リクエストボディの文字列
     */
    private String convertBody() {
        try (StringWriter bodyWriter = new StringWriter();) {
            if (getContentType() != null) {
                writeBody(bodyWriter);
            } else if (body != null) {
                throw new RuntimeException("there was no Content-Type header but body was not empty.");
            }
            bodyWriter.flush();
            return bodyWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException("an error occurred while Writer was being closed.", e);
        }
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
