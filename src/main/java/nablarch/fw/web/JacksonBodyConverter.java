package nablarch.fw.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jacksonを使用してbodyを変換するための{@link BodyConverter}実装クラス。
 */
public class JacksonBodyConverter implements BodyConverter {
    /** 変換可能なMIMEタイプ */
    public static final MediaType CONVERTIBLE_TYPE = new MediaType("application/json");

    /** {@link ObjectMapper} */
    private final ObjectMapper objectMapper;

    /**
     * コンストラクタ。
     */
    public JacksonBodyConverter() {
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
    public boolean isConvertible(Object body, MediaType mediaType) {
        return CONVERTIBLE_TYPE.equals(mediaType);
    }

    @Override
    public String convert(Object body, MediaType mediaType) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("body cannot convert to String. cause[" + e.getMessage() + "].", e);
        }
    }
}
