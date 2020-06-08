package nablarch.fw.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link StringHttpBodyWriter}のテストクラス。
 */
public class StringHttpBodyWriterTest {
    StringHttpBodyWriter sut = new StringHttpBodyWriter();

    /**
     * {@link StringHttpBodyWriter#isWritable(Object, ContentType)}のテスト。
     */
    @Test
    public void isWritableTest() {
        String stringBody = "body";
        int intBody = 1;
        ContentType contentTypePlain = new ContentType("text/plain");
        ContentType contentTypePlainUpperCase = new ContentType("TEXT/PLAIN");
        ContentType contentTypeJsonWithCharset = new ContentType("application/json; charset=utf-8");

        assertTrue(sut.isWritable(stringBody, contentTypePlain));
        assertTrue(sut.isWritable(stringBody, contentTypePlainUpperCase));
        assertTrue(sut.isWritable(stringBody, contentTypeJsonWithCharset));

        assertFalse(sut.isWritable(intBody, contentTypePlain));
        assertFalse(sut.isWritable(intBody, contentTypePlainUpperCase));
        assertFalse(sut.isWritable(intBody, contentTypeJsonWithCharset));
    }

    /**
     * {@link StringHttpBodyWriter#writeValueAsString(Object, ContentType)}のテスト。
     */
    @Test
    public void writeTest() {
        String body = "test body";
        ContentType contentTypePlain = new ContentType("text/plain");
        assertEquals(body, sut.writeValueAsString(body, contentTypePlain));
    }
}