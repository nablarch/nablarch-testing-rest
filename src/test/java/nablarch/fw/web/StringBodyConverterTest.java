package nablarch.fw.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link StringBodyConverter}のテストクラス。
 */
public class StringBodyConverterTest {
    StringBodyConverter sut = new StringBodyConverter();

    /**
     * {@link StringBodyConverter#isConvertible(Object, RestTestMediaType)}のテスト。
     */
    @Test
    public void isWritableTest() {
        String stringBody = "body";
        int intBody = 1;
        RestTestMediaType mediaTypePlain = new RestTestMediaType("text/plain");
        RestTestMediaType mediaTypePlainUpperCase = new RestTestMediaType("TEXT/PLAIN");
        RestTestMediaType mediaTypeJsonWithCharset = new RestTestMediaType("application/json; charset=utf-8");

        assertTrue(sut.isConvertible(stringBody, mediaTypePlain));
        assertTrue(sut.isConvertible(stringBody, mediaTypePlainUpperCase));
        assertTrue(sut.isConvertible(stringBody, mediaTypeJsonWithCharset));

        assertFalse(sut.isConvertible(intBody, mediaTypePlain));
        assertFalse(sut.isConvertible(intBody, mediaTypePlainUpperCase));
        assertFalse(sut.isConvertible(intBody, mediaTypeJsonWithCharset));
    }

    /**
     * {@link StringBodyConverter#convert(Object, RestTestMediaType)}のテスト。
     */
    @Test
    public void writeTest() {
        String body = "test body";
        RestTestMediaType mediaTypePlain = new RestTestMediaType("text/plain");
        assertEquals(body, sut.convert(body, mediaTypePlain));
    }
}