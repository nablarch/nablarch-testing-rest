package nablarch.fw.web;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link String}型のbodyを書き込むための{@link HttpBodyWriter}実装クラス。
 */
public class StringHttpBodyWriter implements HttpBodyWriter {

    @Override
    public boolean isWritable(Object body, String contentType) {
        return body instanceof String &&
                (contentType.toLowerCase().contains("text/plain")
                        || contentType.toLowerCase().contains("application/json"));
    }

    @Override
    public void write(Object body, String contentType, Writer out) throws IOException {
        out.write((String) body);
    }
}
