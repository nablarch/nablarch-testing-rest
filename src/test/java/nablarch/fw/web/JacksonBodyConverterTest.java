package nablarch.fw.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.web.RestTestBodyConverter.MediaType;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link JacksonBodyConverter}のテストクラス。
 */
public class JacksonBodyConverterTest {
    private final JacksonBodyConverter sut = new JacksonBodyConverter();

    /**
     * {@link JacksonBodyConverter#isConvertible(Object, MediaType)}のテスト。
     */
    @Test
    public void testIsConvertible() {
        String stringBody = "body";
        TestDto dto = new TestDto("test body");
        MediaType mediaTypeJson = new MediaType("application/json");
        MediaType mediaTypeJsonUpperCase = new MediaType("APPLICATION/JSON");
        MediaType mediaTypeJsonWithCharset = new MediaType("application/json; charset=utf-8");
        MediaType mediaTypeJsonP = new MediaType("application/json-p");
        MediaType mediaTypePlain = new MediaType("text/plain");
        MediaType mediaTypeHtml = new MediaType("text/html");

        assertTrue(sut.isConvertible(dto, mediaTypeJson));
        assertTrue(sut.isConvertible(dto, mediaTypeJsonUpperCase));
        assertTrue(sut.isConvertible(dto, mediaTypeJsonWithCharset));
        assertTrue(sut.isConvertible(stringBody, mediaTypeJson));
        assertTrue(sut.isConvertible(stringBody, mediaTypeJsonUpperCase));
        assertTrue(sut.isConvertible(stringBody, mediaTypeJsonWithCharset));

        assertFalse(sut.isConvertible(dto, mediaTypeJsonP));
        assertFalse(sut.isConvertible(dto, mediaTypePlain));
        assertFalse(sut.isConvertible(dto, mediaTypeHtml));
        assertFalse(sut.isConvertible(stringBody, mediaTypeJsonP));
        assertFalse(sut.isConvertible(stringBody, mediaTypePlain));
        assertFalse(sut.isConvertible(stringBody, mediaTypeHtml));
    }

    /**
     * {@link JacksonBodyConverter#convert(Object, MediaType)}のテスト。
     * JavaオブジェクトがJSON形式で書き出されることをテストする。
     */
    @Test
    public void testConvert() {
        TestDto dto = new TestDto("test body", "value");
        MediaType mediaTypeJson = new MediaType("application/json");
        assertEquals("{\"field\":\"test body\",\"propertyName\":\"value\"}"
                , sut.convert(dto, mediaTypeJson));
    }

    /**
     * {@link JacksonBodyConverter#convert(Object, MediaType)}のテスト。
     * JavaオブジェクトがJSON形式で書き出され、かつ
     * プロパティにマルチバイト文字を持つ場合エスケープされることをテストする。
     */
    @Test
    public void testConvertEscapeNonAscii() {
        TestDto dto = new TestDto("テスト", "バリュー");
        MediaType mediaTypeJson = new MediaType("application/json");
        assertEquals("{\"field\":\"\\u30C6\\u30B9\\u30C8\",\"propertyName\":\"\\u30D0\\u30EA\\u30E5\\u30FC\"}"
                , sut.convert(dto, mediaTypeJson));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * {@link JacksonBodyConverter#convert(Object, MediaType)}のテスト。
     * {@link ObjectMapper#writeValueAsString(Object)}で例外が発生した場合
     * {@link IllegalArgumentException}が送出されることを確認する。
     */
    @Test
    public void writeValueAsStringThrowsJsonProcessingException(@Mocked final ObjectMapper objectMapper)
            throws JsonProcessingException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("body cannot convert to String. cause[write failed.].");
        expectedException.expectCause(Matchers.<Throwable>instanceOf(DummyJsonException.class));
        TestDto dto = new TestDto("test body", "value");
        MediaType mediaTypeJson = new MediaType("application/json");
        new Expectations() {{
            objectMapper.writeValueAsString(any);
            result = new DummyJsonException("write failed.");
        }};
        sut.convert(dto, mediaTypeJson);
    }

    /**
     * {@link JacksonBodyConverter}のテスト。
     * Factoryクラスを差し替えることでObjectMapperの設定を切り替えられることを確認する。
     */
    @Test
    public void configureTest() {
        JacksonBodyConverter snakeCaseWriter = new SnakeCaseJacksonBodyConverter();
        TestDto dto = new TestDto("test body", "value");
        MediaType mediaTypeJson = new MediaType("application/json");
        assertEquals("{\"field\":\"test body\",\"property_name\":\"value\"}"
                , snakeCaseWriter.convert(dto, mediaTypeJson));
    }

    /**
     * {@link JacksonBodyConverter}の拡張クラス
     * プロパティ名をsnake_caseに変換する。
     */
    private static class SnakeCaseJacksonBodyConverter extends JacksonBodyConverter {
        public SnakeCaseJacksonBodyConverter() {
            super(new SnakeCaseObjectMapperFactory());
        }

        private static class SnakeCaseObjectMapperFactory implements ObjectMapperFactory {
            @Override
            public ObjectMapper create() {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                return objectMapper;
            }
        }
    }

    /**
     * テスト用DTO
     */
    private static class TestDto {
        public TestDto(String field) {
            this.field = field;
        }

        public TestDto(String field, String camelCase) {
            this.field = field;
            this.propertyName = camelCase;
        }

        private String field;

        private String propertyName;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }
    }

    /**
     * テスト用{@link JsonProcessingException}のサブクラス
     */
    private static class DummyJsonException extends JsonProcessingException {
        protected DummyJsonException(String msg) {
            super(msg);
        }
    }
}