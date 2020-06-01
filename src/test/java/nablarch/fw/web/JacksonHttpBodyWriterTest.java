package nablarch.fw.web;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static nablarch.test.Assertion.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JacksonHttpBodyWriterTest {
    JacksonHttpBodyWriter sut = new JacksonHttpBodyWriter();

    @Test
    public void isWritableTest() {
        String stringBody = "body";
        TestDto dto = new TestDto("test body");
        String contentTypeJson = "application/json";
        String contentTypePlain = "text/plain";
        String contentTypeHtml = "text/html";

        assertTrue(sut.isWritable(dto, contentTypeJson));

        assertFalse(sut.isWritable(dto, contentTypePlain));
        assertFalse(sut.isWritable(dto, contentTypeHtml));
        assertFalse(sut.isWritable(stringBody, contentTypeJson));
        assertFalse(sut.isWritable(stringBody, contentTypePlain));
        assertFalse(sut.isWritable(stringBody, contentTypeHtml));
    }

    @Test
    public void writeTest() {
        TestDto dto = new TestDto("test body");
        String contentTypeJson = "application/json";
        StringWriter writer = new StringWriter();
        try {
            sut.write(dto, contentTypeJson, writer);
            writer.close();
            assertEquals("{\"field\":\"test body\"}", writer.toString());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void writeEscapeNonAsciiTest() {
        TestDto dto = new TestDto("テスト");
        String contentTypeJson = "application/json";
        StringWriter writer = new StringWriter();
        try {
            sut.write(dto, contentTypeJson, writer);
            writer.close();
            assertEquals("{\"field\":\"\\u30C6\\u30B9\\u30C8\"}", writer.toString());
        } catch (IOException e) {
            fail(e);
        }
    }

    private static class TestDto {
        public TestDto(String field) {
            this.field = field;
        }

        private String field;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }
}