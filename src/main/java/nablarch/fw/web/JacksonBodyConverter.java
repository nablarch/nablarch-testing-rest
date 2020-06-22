package nablarch.fw.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jacksonを使用してbodyを変換するための{@link BodyConverter}実装クラス。
 */
public class JacksonBodyConverter implements BodyConverter {
    /** 変換可能なMIMEタイプ */
    private static final RestTestMediaType CONVERTIBLE_TYPE = new RestTestMediaType("application/json");

    /** {@link ObjectMapper} */
    private final ObjectMapper objectMapper;

    /**
     * コンストラクタ。
     */
    public JacksonBodyConverter() {
        this(new DefaultObjectMapperFactory());
    }

    public JacksonBodyConverter(ObjectMapperFactory factory) {
        objectMapper = factory.create();
    }

    @Override
    public boolean isConvertible(Object body, RestTestMediaType mediaType) {
        return CONVERTIBLE_TYPE.equals(mediaType);
    }

    @Override
    public String convert(Object body, RestTestMediaType mediaType) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("body cannot convert to String. cause[" + e.getMessage() + "].", e);
        }
    }

    /**
     * {@link ObjectMapper}のfactoryインターフェイス
     */
    public interface ObjectMapperFactory {
        ObjectMapper create();
    }

    /**
     * デフォルト{@link ObjectMapperFactory}実装クラス。
     * NON ASCII文字のエスケープのみ設定変更した{@link ObjectMapper}を生成する。
     */
    private static class DefaultObjectMapperFactory implements ObjectMapperFactory {
        @Override
        public ObjectMapper create() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
            return objectMapper;
        }
    }
}
