package nablarch.fw.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;

/**
 * Jacksonを使用してbodyを書き込むための{@link HttpBodyWriter}実装クラス。
 */
public class JacksonHttpBodyWriter implements HttpBodyWriter {

    /** {@link ObjectMapper} */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isWritable(Object body, String contentType) {
        return !(body instanceof String) && contentType.equals("application/json");
    }

    @Override
    public void write(Object body, String contentType, Writer out) throws IOException {
        objectMapper.writeValue(out, body);
    }
}
