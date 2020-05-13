package nablarch.fw.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link Object}型のbodyを書き込むための{@link HttpBodyWriter}の実装クラス。
 * 対応するContent-Typeは"application/json"
 */
public class JacksonHttpBodyWriter implements HttpBodyWriter {

    /** {@link ObjectMapper} */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isWritable(Object body, String contentType) {
        return contentType.equals("application/json");
    }

    @Override
    public void write(Object body, String contentType, Writer out) throws IOException {
        // JSON文字列として設定された場合はそのまま書き出す
        if (body instanceof String) {
            out.write((String) body);
            return;
        }
        objectMapper.writeValue(out, body);
    }
}
