package nablarch.fw.web;

/**
 * {@link String}型のbodyを返すための{@link BodyConverter}実装クラス。
 */
public class StringBodyConverter implements BodyConverter {

    @Override
    public boolean isConvertible(Object body, MediaType mediaType) {
        return body instanceof String;
    }

    @Override
    public String convert(Object body, MediaType mediaType) {
        return (String) body;
    }
}
