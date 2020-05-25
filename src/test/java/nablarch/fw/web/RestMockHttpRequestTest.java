package nablarch.fw.web;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RestMockHttpRequestTest {
    private static final String LS = "\r\n";
    private static final String DEFAULT_REQUEST = "GET / HTTP/1.1" + LS
            + "Content-Length: 0" + LS
            + LS;
    private static final String HTTP_REQUEST = "POST /test HTTP/1.1" + LS
            + "test: OK" + LS
            + "Content-Length: 9" + LS
            + "Content-Type: application/json" + LS
            + LS
            + "test body";

    @Test
    public void testNormal() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        assertThat(sut.getMethod(), is("GET"));
        assertThat(sut.getRequestUri(), is("/"));
        assertTrue(sut.getHeaderMap().isEmpty());
        assertNull(sut.getContentType());
        assertNull(sut.getBody());
        assertThat(sut.toString(), is(DEFAULT_REQUEST));

        sut.setBody("test body");
        assertThat(sut.getContentType(), is("testType"));
        assertThat(sut.getBody(), is("test body"));

        sut.setContentType("application/json");
        assertThat(sut.getContentType(), is("application/json"));

        Map<String, String> headerMap = sut.getHeaderMap();
        headerMap.put("test", "OK");
        sut.setMethod("POST").setRequestUri("/test").setHeaderMap(headerMap);
        assertThat(sut.toString(), is(HTTP_REQUEST));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAbnormalRequestHasNotContentTypeWithBody() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("there was no Content-Type header but body was not empty.");
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter()), null);
        sut.setBody("test body");
        sut.toString();
    }

    @Test
    public void testAbnormalWriterThrowsIOException() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("an error occurred while Writer was writing body.");
        expectedException.expectCause(allOf(
                instanceOf(IOException.class)
                , hasProperty("message", is("Writer"))
        ));
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new ThrowExceptionMockWriter()), null);
        sut.setBody("test body");
        sut.setContentType("text/plain");
        sut.toString();
    }

    @Test
    public void testAbnormalCouldNotFindBodyWriter() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("unsupported media type requested. Content-Type = [ text/plain ]");
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new NoContentWritableMockWriter()), "text/plain");
        sut.setBody("test body");
        sut.toString();
    }

    private static class MockWriter implements HttpBodyWriter {
        @Override
        public boolean isWritable(Object body, String contentType) {
            return true;
        }

        @Override
        public void write(Object body, String contentType, Writer out) throws IOException {
            out.write((String) body);
        }
    }

    private static class ThrowExceptionMockWriter implements HttpBodyWriter {
        @Override
        public boolean isWritable(Object body, String contentType) {
            return true;
        }

        @Override
        public void write(Object body, String contentType, Writer out) throws IOException {
            throw new IOException("Writer");
        }
    }

    private static class NoContentWritableMockWriter implements HttpBodyWriter {

        @Override
        public boolean isWritable(Object body, String contentType) {
            return false;
        }

        @Override
        public void write(Object body, String contentType, Writer out) throws IOException {
            // NOP
        }
    }
}