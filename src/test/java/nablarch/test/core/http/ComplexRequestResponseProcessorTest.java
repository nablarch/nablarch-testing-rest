package nablarch.test.core.http;

import nablarch.fw.test.MockConverter;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.MockHttpRequest;
import nablarch.fw.web.RestMockHttpRequest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * {@link ComplexRequestResponseProcessor}のテスト
 */
public class ComplexRequestResponseProcessorTest {
    @Test
    public void testSingleProcessor() {
        NablarchSIDManager sidManager = new NablarchSIDManager();
        List<RequestResponseProcessor> processors = Collections.<RequestResponseProcessor>singletonList(sidManager);

        ComplexRequestResponseProcessor sut = new ComplexRequestResponseProcessor();
        sut.setProcessors(processors);

        HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
        HttpResponse response = new HttpResponse();
        response.setHeader("Set-Cookie", "NABLARCH_SID=nablarch_sid");

        sut.processResponse(new MockHttpRequest(), response);
        sut.processRequest(request);
        assertThat(request.getHeader("Cookie"), is("NABLARCH_SID=nablarch_sid"));
    }

    @Test
    public void testMultipleProcessors() {
        NablarchSIDManager sidManager = new NablarchSIDManager();
        TestProcessor testProcessor = new TestProcessor();
        List<RequestResponseProcessor> processors = Arrays.asList(sidManager, testProcessor);

        ComplexRequestResponseProcessor sut = new ComplexRequestResponseProcessor();
        sut.setProcessors(processors);

        HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
        HttpResponse response = new HttpResponse();
        response.setHeader("Set-Cookie", "NABLARCH_SID=nablarch_sid");

        sut.processResponse(new MockHttpRequest(), response);
        sut.processRequest(request);
        assertThat(request.getHeader("Cookie"), is("NABLARCH_SID=nablarch_sid"));
        assertThat(request.getHeader("test"), is("processor"));
        assertThat(response.getStatusCode(), is(404));
    }

    @Test
    public void testReset() {
        NablarchSIDManager sidManager = new NablarchSIDManager();
        TestProcessor testProcessor = new TestProcessor();
        List<RequestResponseProcessor> processors = Arrays.asList(sidManager, testProcessor);

        ComplexRequestResponseProcessor sut = new ComplexRequestResponseProcessor();
        sut.setProcessors(processors);

        HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
        HttpResponse response = new HttpResponse();
        response.setHeader("Set-Cookie", "NABLARCH_SID=nablarch_sid");

        sut.processResponse(new MockHttpRequest(), response);
        sut.reset();
        sut.processRequest(request);
        assertNull(request.getHeader("Cookie"));
        assertNull(request.getHeader("test"));
    }

    private static class TestProcessor implements RequestResponseProcessor {
        private String processor;

        @Override
        public HttpRequest processRequest(HttpRequest request) {
            if (processor != null) {
                request.getHeaderMap().put("test", processor);
            }
            return request;
        }

        @Override
        public HttpResponse processResponse(HttpRequest request, HttpResponse response) {
            processor = "processor";
            return response.setStatusCode(404);
        }

        @Override
        public void reset() {
            processor = null;
        }
    }
}