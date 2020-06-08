package nablarch.fw.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import mockit.Expectations;
import mockit.Mocked;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link JacksonHttpBodyWriter}のテストクラス。
 */
public class JacksonHttpBodyWriterTest {
    private final JacksonHttpBodyWriter sut = new JacksonHttpBodyWriter();

    /**
     * {@link JacksonHttpBodyWriter#isWritable(Object, ContentType)}のテスト。
     */
    @Test
    public void isWritableTest() {
        String stringBody = "body";
        TestDto dto = new TestDto("test body");
        ContentType contentTypeJson = new ContentType("application/json");
        ContentType contentTypeJsonP = new ContentType("application/json-p");
        ContentType contentTypeJsonUpperCase = new ContentType("APPLICATION/JSON");
        ContentType contentTypeJsonWithCharset = new ContentType("application/json; charset=utf-8");
        ContentType contentTypePlain = new ContentType("text/plain");
        ContentType contentTypeHtml = new ContentType("text/html");

        assertTrue(sut.isWritable(dto, contentTypeJson));
        assertTrue(sut.isWritable(dto, contentTypeJsonUpperCase));
        assertTrue(sut.isWritable(dto, contentTypeJsonWithCharset));
        assertTrue(sut.isWritable(stringBody, contentTypeJson));
        assertTrue(sut.isWritable(stringBody, contentTypeJsonUpperCase));
        assertTrue(sut.isWritable(stringBody, contentTypeJsonWithCharset));

        assertFalse(sut.isWritable(dto, contentTypePlain));
        assertFalse(sut.isWritable(dto, contentTypeHtml));
        assertFalse(sut.isWritable(dto, contentTypeJsonP));
        assertFalse(sut.isWritable(stringBody, contentTypePlain));
        assertFalse(sut.isWritable(stringBody, contentTypeHtml));
        assertFalse(sut.isWritable(stringBody, contentTypeJsonP));
    }

    /**
     * {@link JacksonHttpBodyWriter#writeValueAsString(Object, ContentType)}のテスト。
     * JavaオブジェクトがJSON形式で書き出されることをテストする。
     */
    @Test
    public void writeTest() {
        TestDto dto = new TestDto("test body", "value");
        ContentType contentTypeJson = new ContentType("application/json");
        assertEquals("{\"field\":\"test body\",\"propertyName\":\"value\"}"
                , sut.writeValueAsString(dto, contentTypeJson));
    }

    /**
     * {@link JacksonHttpBodyWriter#writeValueAsString(Object, ContentType)}のテスト。
     * JavaオブジェクトがJSON形式で書き出され、かつ
     * プロパティにマルチバイト文字を持つ場合エスケープされることをテストする。
     */
    @Test
    public void writeEscapeNonAsciiTest() {
        TestDto dto = new TestDto("テスト", "バリュー");
        ContentType contentTypeJson = new ContentType("application/json");
        assertEquals("{\"field\":\"\\u30C6\\u30B9\\u30C8\",\"propertyName\":\"\\u30D0\\u30EA\\u30E5\\u30FC\"}"
                , sut.writeValueAsString(dto, contentTypeJson));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * {@link JacksonHttpBodyWriter#writeValueAsString(Object, ContentType)}のテスト。
     * {@link ObjectMapper#writeValueAsString(Object)}で例外が発生した場合
     * {@link IllegalArgumentException}が送出されることを確認する。
     */
    @Test
    public void writeValueAsStringThrowsJsonProcessingException(@Mocked final ObjectMapper objectMapper) {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("body cannot write as String. cause[write failed.].");
        expectedException.expectCause(Matchers.<Throwable>instanceOf(DummyJsonException.class));
        try {
            TestDto dto = new TestDto("test body", "value");
            ContentType contentTypeJson = new ContentType("application/json");
            new Expectations() {{
                objectMapper.writeValueAsString(any);
                result = new DummyJsonException("write failed.");
            }};
            sut.writeValueAsString(dto, contentTypeJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@link JacksonHttpBodyWriter#configure(ObjectMapper)}のテスト。
     * ObjectMapperの設定を切り替えられることを確認する。
     */
    @Test
    public void configureTest() {
        JacksonHttpBodyWriter snakeCaseWriter = new JacksonHttpBodyWriter() {
            @Override
            protected void configure(ObjectMapper objectMapper) {
                super.configure(objectMapper);
                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            }
        };
        TestDto dto = new TestDto("test body", "value");
        ContentType contentTypeJson = new ContentType("application/json");
        assertEquals("{\"field\":\"test body\",\"property_name\":\"value\"}"
                , snakeCaseWriter.writeValueAsString(dto, contentTypeJson));
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