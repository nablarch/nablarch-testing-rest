package nablarch.fw.web;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ContentTypeTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_PLAIN_UPPER_CASE = "TEXT/PLAIN";
    private static final String TEXT_PLAIN_WITH_SPACE = " TEXT/plain ";
    private static final String MULTIPART = "multipart/form-data";
    private static final String CHARSET = "charset=UTF8";
    private static final String BOUNDARY = "boundary=aBoundary";

    @Test
    public void testConstructorNullArgShouldThrowException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("content type must not be empty.");
        new ContentType(null);
    }

    @Test
    public void testConstructorEmptyArgShouldThrowException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("content type must not be empty.");
        new ContentType("");
    }

    @Test
    public void testConstructorInvalidArgShouldThrowException() {
        String rawContentType = TEXT_PLAIN + "; " + CHARSET + "; " + BOUNDARY;
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("[" + rawContentType + "] is invalid format.");
        new ContentType(rawContentType);
    }

    @Test
    public void testPlainContent() {
        ContentType sut = new ContentType(TEXT_PLAIN);
        assertTrue(sut.is(TEXT_PLAIN));
        assertTrue(sut.is(TEXT_PLAIN_UPPER_CASE));
        assertTrue(sut.is(TEXT_PLAIN_WITH_SPACE));
        assertEquals(TEXT_PLAIN, sut.getMediaType());
        assertNull(sut.getCharset());
        assertNull(sut.getBoundary());
        assertEquals(TEXT_PLAIN, sut.toString());
    }

    @Test
    public void testPlainContentUpperCase() {
        ContentType sut = new ContentType(TEXT_PLAIN_UPPER_CASE);
        assertTrue(sut.is(TEXT_PLAIN));
        assertTrue(sut.is(TEXT_PLAIN_UPPER_CASE));
        assertTrue(sut.is(TEXT_PLAIN_WITH_SPACE));
        assertEquals(TEXT_PLAIN_UPPER_CASE, sut.getMediaType());
        assertNull(sut.getCharset());
        assertNull(sut.getBoundary());
        assertEquals(TEXT_PLAIN_UPPER_CASE, sut.toString());
    }

    @Test
    public void testPlainContentWithCharset() {
        ContentType sut = new ContentType(TEXT_PLAIN + ";" + CHARSET);
        assertTrue(sut.is(TEXT_PLAIN));
        assertEquals(TEXT_PLAIN, sut.getMediaType());
        assertEquals(CHARSET, sut.getCharset());
        assertNull(sut.getBoundary());
        assertEquals(TEXT_PLAIN + "; " + CHARSET, sut.toString());
    }

    @Test
    public void testMultipartWithBoundary() {
        ContentType sut = new ContentType(MULTIPART + ";" + BOUNDARY);
        assertTrue(sut.is(MULTIPART));
        assertEquals(MULTIPART, sut.getMediaType());
        assertNull(sut.getCharset());
        assertEquals(BOUNDARY, sut.getBoundary());
        assertEquals(MULTIPART + "; " + BOUNDARY, sut.toString());
    }
}