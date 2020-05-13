package nablarch.fw.web;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link String}型のbodyを書き込むための{@link HttpBodyWriter}の実装クラス。
 * 対応するContent-Typeは"text/plain"
 */
public class StringHttpBodyWriter implements HttpBodyWriter {

    @Override
    public boolean isWritable(Object body, String contentType) {
        return body instanceof String && contentType.equals("text/plain");
    }

    @Override
    public void write(Object body, String contentType, Writer out) throws IOException {
        out.write((String) body);
    }
}
