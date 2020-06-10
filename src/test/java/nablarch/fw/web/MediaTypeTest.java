package nablarch.fw.web;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MediaTypeTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_PLAIN_UPPER_CASE = "TEXT/PLAIN";
    private static final String TEXT_PLAIN_WITH_SPACE = " TEXT/plain ";
    private static final String MULTIPART = "multipart/form-data";
    private static final MediaType TEXT_PLAIN_TYPE = new MediaType(TEXT_PLAIN);
    private static final MediaType TEXT_PLAIN_UPPER_CASE_TYPE = new MediaType(TEXT_PLAIN_UPPER_CASE);
    private static final MediaType TEXT_PLAIN_WITH_SPACE_TYPE = new MediaType(TEXT_PLAIN_WITH_SPACE);
    private static final MediaType MULTIPART_TYPE = new MediaType(MULTIPART);
    private static final String CHARSET = "charset=UTF8";
    private static final String BOUNDARY = "boundary=aBoundary";

    @Test
    public void testConstructorNullArgShouldThrowException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("content type must not be empty.");
        new MediaType(null);
    }

    @Test
    public void testConstructorEmptyArgShouldThrowException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("content type must not be empty.");
        new MediaType("");
    }

    @Test
    public void testPlainContent() {
        MediaType sut = new MediaType(TEXT_PLAIN);
        assertEquals(TEXT_PLAIN_TYPE, sut);
        assertEquals(TEXT_PLAIN_UPPER_CASE_TYPE, sut);
        assertEquals(TEXT_PLAIN_WITH_SPACE_TYPE, sut);
    }

    @Test
    public void testPlainContentUpperCase() {
        MediaType sut = new MediaType(TEXT_PLAIN_UPPER_CASE);
        assertEquals(TEXT_PLAIN_TYPE, sut);
        assertEquals(TEXT_PLAIN_UPPER_CASE_TYPE, sut);
        assertEquals(TEXT_PLAIN_WITH_SPACE_TYPE, sut);
    }

    @Test
    public void testPlainContentWithCharset() {
        MediaType sut = new MediaType(TEXT_PLAIN + ";" + CHARSET);
        assertEquals(TEXT_PLAIN_TYPE, sut);
    }

    @Test
    public void testMultipartWithBoundary() {
        MediaType sut = new MediaType(MULTIPART + ";" + BOUNDARY);
        assertEquals(MULTIPART_TYPE, sut);
    }

    @Test
    public void testOverriddenEqualsAndHashCode() {
        MediaType x = new MediaType("content_type");
        MediaType y = new MediaType("CONTENT_TYPE");
        MediaType z = new MediaType("Content_Type");
        MediaType other = new MediaType("other");

        assertTrue(x.equals(x));
        assertTrue(x.equals(y) && y.equals(x));
        assertTrue(x.equals(y) && y.equals(z) && z.equals(x));

        assertFalse(x.equals(null));

        assertEquals(x.hashCode(), y.hashCode());
        assertEquals(y.hashCode(), z.hashCode());
        assertEquals(z.hashCode(), x.hashCode());

        assertNotEquals(x, other);
        assertNotEquals(x.hashCode(), other.hashCode());
    }
}