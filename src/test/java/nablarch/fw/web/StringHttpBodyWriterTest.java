package nablarch.fw.web;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static nablarch.test.Assertion.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringHttpBodyWriterTest {
    StringHttpBodyWriter sut = new StringHttpBodyWriter();

    @Test
    public void isWritableTest() {
        String stringBody = "body";
        int intBody = 1;
        String contentTypePlain = "text/plain";
        String contentTypeJson = "application/json";
        String contentTypeHtml = "text/html";

        assertTrue(sut.isWritable(stringBody, contentTypePlain));
        assertTrue(sut.isWritable(stringBody, contentTypeJson));

        assertFalse(sut.isWritable(stringBody, contentTypeHtml));
        assertFalse(sut.isWritable(intBody, contentTypePlain));
        assertFalse(sut.isWritable(intBody, contentTypeJson));
        assertFalse(sut.isWritable(intBody, contentTypeHtml));
    }

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