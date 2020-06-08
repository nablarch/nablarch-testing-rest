package nablarch.fw.web;

/**
 * {@link String}型のbodyを書き込むための{@link HttpBodyWriter}実装クラス。
 */
public class StringHttpBodyWriter implements HttpBodyWriter {

    @Override
    public boolean isWritable(Object body, ContentType contentType) {
        return body instanceof String;
    }

    @Override
    public String writeValueAsString(Object body, ContentType contentType) {
        return (String) body;
    }
}
