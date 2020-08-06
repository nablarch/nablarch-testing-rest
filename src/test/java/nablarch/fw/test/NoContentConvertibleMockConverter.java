package nablarch.fw.test;

import nablarch.fw.web.RestTestBodyConverter;

/**
 * テスト用の{@link RestTestBodyConverter}実装。
 * {@link RestTestBodyConverter#isConvertible(Object, MediaType)}で常にfalseを返す。
 */
public class NoContentConvertibleMockConverter implements RestTestBodyConverter {

    @Override
    public boolean isConvertible(Object body, MediaType mediaType) {
        return false;
    }

    @Override
    public String convert(Object body, MediaType mediaType) {
        return null;
    }
}

