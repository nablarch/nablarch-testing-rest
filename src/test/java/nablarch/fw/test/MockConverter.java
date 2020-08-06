package nablarch.fw.test;

import nablarch.fw.web.RestTestBodyConverter;

/**
 * テスト用の{@link RestTestBodyConverter}実装。
 * Content-Typeが何であってもボディをStringにキャストして返す。
 */
public class MockConverter implements RestTestBodyConverter {
    @Override
    public boolean isConvertible(Object body, MediaType mediaType) {
        return true;
    }

    @Override
    public String convert(Object body, MediaType mediaType) {
        return (String) body;
    }
}
