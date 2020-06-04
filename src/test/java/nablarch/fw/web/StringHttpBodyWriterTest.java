package nablarch.fw.web;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static nablarch.test.Assertion.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link StringHttpBodyWriter}のテストクラス。
 */
public class StringHttpBodyWriterTest {
    StringHttpBodyWriter sut = new StringHttpBodyWriter();

    /**
     * {@link StringHttpBodyWriter#isWritable(Object, String)}のテスト。
     */
    @Test
    public void isWritableTest() {
        String stringBody = "body";
        int intBody = 1;
        String contentTypePlain = "text/plain";
        String contentTypePlainUpperCase = "TEXT/PLAIN";
        String contentTypeJson = "application/json";
        String contentTypeJsonUpperCase = "APPLICATION/JSON";
        String contentTypeJsonWithCharset = "application/json; charset=utf-8";
        String contentTypeHtml = "text/html";

        assertTrue(sut.isWritable(stringBody, contentTypePlain));
        assertTrue(sut.isWritable(stringBody, contentTypePlainUpperCase));
        assertTrue(sut.isWritable(stringBody, contentTypeJson));
        assertTrue(sut.isWritable(stringBody, contentTypeJsonUpperCase));
        assertTrue(sut.isWritable(stringBody, contentTypeJsonWithCharset));

        assertFalse(sut.isWritable(stringBody, contentTypeHtml));
        assertFalse(sut.isWritable(intBody, contentTypePlain));
        assertFalse(sut.isWritable(intBody, contentTypePlainUpperCase));
        assertFalse(sut.isWritable(intBody, contentTypeJson));
        assertFalse(sut.isWritable(intBody, contentTypeJsonUpperCase));
        assertFalse(sut.isWritable(intBody, contentTypeJsonWithCharset));
        assertFalse(sut.isWritable(intBody, contentTypeHtml));
    }

    /**
     * {@link StringHttpBodyWriter#write(Object, String, Writer)}のテスト。
     */
    @Test
    public void writeTest() {
        String body = "test body";
        String contentTypePlain = "text/plain";
        StringWriter writer = new StringWriter();
        try {
            sut.write(body, contentTypePlain, writer);
            writer.close();
            assertEquals(body, writer.toString());
        } catch (IOException e) {
            fail(e);
        }
    }
}