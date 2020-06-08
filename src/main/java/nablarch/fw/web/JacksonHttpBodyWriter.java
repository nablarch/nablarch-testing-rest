package nablarch.fw.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public boolean isWritable(Object body, ContentType contentType) {
        return contentType.is("application/json");
    }

    @Override
    public String writeValueAsString(Object body, ContentType contentType) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("body cannot write as String. cause[" + e.getMessage() + "].", e);
        }
    }
}
