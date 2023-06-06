package nablarch.test.core.http;

import nablarch.fw.test.MockConverter;
import nablarch.fw.web.HttpCookie;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.MockHttpRequest;
import nablarch.fw.web.RestMockHttpRequest;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * {@link RequestResponseCookieManager}のテスト
 */
public class RequestResponseCookieManagerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testHasSID() throws UnsupportedEncodingException {

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
            , "testType");
            HttpResponse response = new HttpResponse();
            HttpCookie httpCookie = new HttpCookie();
            httpCookie.put("JSESSIONID", "jsessionid");
            response.addCookie(httpCookie);

            RequestResponseCookieManager sut = new RequestResponseCookieManager();
            sut.setCookieName("JSESSIONID");
            sut.processResponse(new MockHttpRequest(), response);
            sut.processRequest(request);
            assertThat(request.getHeader("Cookie"), is("JSESSIONID=jsessionid"));

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, allOf(
                containsString("Get cookie: JSESSIONID = jsessionid"),
                containsString("Set cookie: JSESSIONID = jsessionid")));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testReset() throws UnsupportedEncodingException {

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
            HttpResponse response = new HttpResponse();
            HttpCookie httpCookie = new HttpCookie();
            httpCookie.put("JSESSIONID", "jsessionid");
            response.addCookie(httpCookie);

            RequestResponseCookieManager sut = new RequestResponseCookieManager();
            sut.setCookieName("JSESSIONID");
            sut.processResponse(new MockHttpRequest(), response);
            sut.reset();
            sut.processRequest(request);
            assertNull(request.getHeader("Cookie"));

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, containsString("Get cookie: JSESSIONID = jsessionid"));
            assertThat(logText, not(containsString("Set cookie: JSESSIONID = jsessionid")));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testCookieNotSet() throws UnsupportedEncodingException {

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            RequestResponseCookieManager sut = new RequestResponseCookieManager();
            sut.setCookieName("JSESSIONID");
            sut.processResponse(new MockHttpRequest(), new HttpResponse());

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, containsString("Set-Cookie header value does not contain JSESSIONID."));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testCookieNameNotSet() throws UnsupportedEncodingException {

        expectedException.expect(Matchers.allOf(
            Matchers.instanceOf(IllegalStateException.class),
            Matchers.hasProperty("message", Matchers.is("cookieName must be set."))
        ));

        RequestResponseCookieManager sut = new RequestResponseCookieManager();
        sut.processResponse(new MockHttpRequest(), new HttpResponse());
    }

    @Test
    public void testEmptyCookie() throws UnsupportedEncodingException {
        HttpResponse response = new HttpResponse();

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            RequestResponseCookieManager sut = new RequestResponseCookieManager();
            sut.setCookieName("JSESSIONID");
            sut.processResponse(new MockHttpRequest(), response);

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, containsString("Set-Cookie header value does not contain JSESSIONID."));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testHasNotSID() throws UnsupportedEncodingException {

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            HttpResponse response = new HttpResponse();
            HttpCookie httpCookie = new HttpCookie();
            httpCookie.put("cookie", "abc");
            response.addCookie(httpCookie);

            RequestResponseCookieManager sut = new RequestResponseCookieManager();
            sut.setCookieName("JSESSIONID");
            sut.processResponse(new MockHttpRequest(), response);

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, containsString("Set-Cookie header value does not contain JSESSIONID."));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testNotRestMockHttpRequest() {
        MockHttpRequest mockHttpRequest = new MockHttpRequest();
        NablarchSIDManager sut = new NablarchSIDManager();
        HttpRequest processedRequest = sut.processRequest(mockHttpRequest);
        assertNull(processedRequest.getHeader("Cookie"));
    }

}