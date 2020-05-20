package nablarch.fw.web;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static nablarch.test.Assertion.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class RestMockHttpRequestBuilderTest {
    RestMockHttpRequestBuilder sut = new RestMockHttpRequestBuilder();

    @Test
    public void bodyWriterTest() {
        try {
            Field writersField = sut.getClass().getDeclaredField("httpBodyWriters");
            writersField.setAccessible(true);
            List<HttpBodyWriter> defaultList = (List<HttpBodyWriter>) writersField.get(sut);
            assertEquals(2, defaultList.size());
            defaultList.forEach(writer -> {
                if (!(writer instanceof StringHttpBodyWriter)
                        && !(writer instanceof JacksonHttpBodyWriter)) {
                    fail("default HttpBodyWriter list has unknown Writer.");
                }
            });

            List<HttpBodyWriter> bodyWriters = Arrays.asList(new StringHttpBodyWriter(), new JacksonHttpBodyWriter());
            sut.setHttpBodyWriters(bodyWriters);
            List<HttpBodyWriter> actual = (List<HttpBodyWriter>) writersField.get(sut);
            assertThat(actual, is(bodyWriters));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }

    @Test
    public void defaultContentTypeTest() {
        try {
            Field defaultContentTypeField = sut.getClass().getDeclaredField("defaultContentType");
            defaultContentTypeField.setAccessible(true);
            String defaultValue = (String) defaultContentTypeField.get(sut);
            assertThat(defaultValue, is("application/json"));

            sut.setDefaultContentType("text/plain");
            String textPlain = (String) defaultContentTypeField.get(sut);
            assertThat(textPlain, is("text/plain"));

        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }

    @Test
    public void newRequestTest() {
        RestMockHttpRequest getReq = sut.get("test");
        assertThat(getReq.getMethod(), is("GET"));
        assertThat(getReq.getRequestUri(), is("test"));
        assertNull(getReq.getContentType());

        RestMockHttpRequest postReq = sut.post("test");
        assertThat(postReq.getMethod(), is("POST"));
        assertThat(getReq.getRequestUri(), is("test"));

        RestMockHttpRequest putReq = sut.put("test");
        assertThat(putReq.getMethod(), is("PUT"));
        assertThat(putReq.getRequestUri(), is("test"));

        RestMockHttpRequest deleteReq = sut.delete("test");
        assertThat(deleteReq.getMethod(), is("DELETE"));
        assertThat(deleteReq.getRequestUri(), is("test"));
    }
}