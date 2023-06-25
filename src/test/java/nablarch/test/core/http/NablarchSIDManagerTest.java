package nablarch.test.core.http;

import nablarch.fw.test.MockConverter;
import nablarch.fw.web.HttpCookie;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.MockHttpRequest;
import nablarch.fw.web.RestMockHttpRequest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * {@link NablarchSIDManager}のテスト
 */
public class NablarchSIDManagerTest {
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
            httpCookie.put("NABLARCH_SID", "nablarch_sid");
            response.addCookie(httpCookie);

            NablarchSIDManager sut = new NablarchSIDManager();
            sut.processResponse(new MockHttpRequest(), response);
            sut.processRequest(request);
            assertThat(request.getHeader("Cookie"), is("NABLARCH_SID=nablarch_sid"));

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, allOf(
                    containsString("Get cookie: NABLARCH_SID = nablarch_sid"),
                    containsString("Set cookie: NABLARCH_SID = nablarch_sid")));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testChangeCookieName() throws UnsupportedEncodingException {

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
            HttpResponse response = new HttpResponse();
            HttpCookie httpCookie = new HttpCookie();
            httpCookie.put("ANOTHER_SID", "nablarch_sid");
            response.addCookie(httpCookie);

            NablarchSIDManager sut = new NablarchSIDManager();
            sut.setCookieName("ANOTHER_SID");
            sut.processResponse(new MockHttpRequest(), response);
            sut.processRequest(request);
            assertThat(request.getHeader("Cookie"), is("ANOTHER_SID=nablarch_sid"));

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, allOf(
                containsString("Get cookie: ANOTHER_SID = nablarch_sid"),
                containsString("Set cookie: ANOTHER_SID = nablarch_sid")));
            assertThat(logText, not(containsString("Set-Cookie header value does not contain NABLARCH_SID.")));
        } finally {
            System.setOut(originalStdOut);
        }
    }

}