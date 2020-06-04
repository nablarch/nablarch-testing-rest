package nablarch.fw.web;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;

/**
 * Jacksonを使用してbodyを書き込むための{@link HttpBodyWriter}実装クラス。
 */
public class JacksonHttpBodyWriter implements HttpBodyWriter {

    /** {@link ObjectMapper} */
    private final ObjectMapper objectMapper;

    /**
     * コンストラクタ。
     */
    public JacksonHttpBodyWriter() {
        objectMapper = new ObjectMapper();
        configure(objectMapper);
    }

    /**
     * {@link ObjectMapper}に対するオプション設定などを行う。
     * このクラスではNON_ASCII文字のエスケープ設定を行う。
     *
     * @param objectMapper {@link ObjectMapper}
     */
    protected void configure(ObjectMapper objectMapper) {
        objectMapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
    }

    @Override
    public boolean isWritable(Object body, String contentType) {
        return !(body instanceof String) && contentType.toLowerCase().contains("application/json");
    }

    @Override
    public void write(Object body, String contentType, Writer out) throws IOException {
        objectMapper.writeValue(out, body);
    }
}
