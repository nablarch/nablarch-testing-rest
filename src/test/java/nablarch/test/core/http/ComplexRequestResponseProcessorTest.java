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
import static org.junit.Assert.assertThat;

/**
 * {@link ComplexRequestResponseProcessorFactory.ComplexRequestResponseProcessor}のテスト
 */
public class ComplexRequestResponseProcessorTest {
    @Test
    public void testSingleProcessor() {
        NablarchSIDManagerFactory sidManagerFactory = new NablarchSIDManagerFactory();
        List<RequestResponseProcessorFactory> processors = Collections.<RequestResponseProcessorFactory>singletonList(sidManagerFactory);
        ComplexRequestResponseProcessorFactory complexRequestResponseProcessorFactory = new ComplexRequestResponseProcessorFactory();
        complexRequestResponseProcessorFactory.setProcessorFactories(processors);

        ComplexRequestResponseProcessorFactory.ComplexRequestResponseProcessor sut =
                (ComplexRequestResponseProcessorFactory.ComplexRequestResponseProcessor) complexRequestResponseProcessorFactory.create();

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
        NablarchSIDManagerFactory sidManagerFactory = new NablarchSIDManagerFactory();
        TestProcessorFactory testProcessorFactory = new TestProcessorFactory();
        List<RequestResponseProcessorFactory> processors = Arrays.asList(sidManagerFactory, testProcessorFactory);

        ComplexRequestResponseProcessorFactory complexRequestResponseProcessorFactory = new ComplexRequestResponseProcessorFactory();
        complexRequestResponseProcessorFactory.setProcessorFactories(processors);
        ComplexRequestResponseProcessorFactory.ComplexRequestResponseProcessor sut =
                (ComplexRequestResponseProcessorFactory.ComplexRequestResponseProcessor) complexRequestResponseProcessorFactory.create();


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

    private static class TestProcessorFactory implements RequestResponseProcessorFactory {
        @Override
        public RequestResponseProcessor create() {
            return new TestProcessor();
        }

        private static class TestProcessor implements RequestResponseProcessor {
            @Override
            public HttpRequest processRequest(HttpRequest request) {
                request.getHeaderMap().put("test", "processor");
                return request;
            }

            @Override
            public HttpResponse processResponse(HttpRequest request, HttpResponse response) {
                return response.setStatusCode(404);
            }
        }
    }
}