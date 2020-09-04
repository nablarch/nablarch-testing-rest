package nablarch.test.core.http;

import nablarch.fw.test.MockConverter;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * {@link NablarchSIDManager}のテスト
 */
public class NablarchSIDManagerTest {
    @Test
    public void testHasSID() throws UnsupportedEncodingException {
        HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
        HttpResponse response = new HttpResponse();
        response.setHeader("Set-Cookie", "NABLARCH_SID=nablarch_sid");

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            NablarchSIDManager sut = new NablarchSIDManager();
            sut.processResponse(new MockHttpRequest(), response);
            sut.processRequest(request);
            assertThat(request.getHeader("Cookie"), is("NABLARCH_SID=nablarch_sid"));

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, allOf(
                    containsString("Get session ID: NABLARCH_SID = nablarch_sid"),
                    containsString("Set session ID: NABLARCH_SID = nablarch_sid")));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testReset() throws UnsupportedEncodingException {
        HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
        HttpResponse response = new HttpResponse();
        response.setHeader("Set-Cookie", "NABLARCH_SID=nablarch_sid");

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            NablarchSIDManager sut = new NablarchSIDManager();
            sut.processResponse(new MockHttpRequest(), response);
            sut.reset();
            sut.processRequest(request);
            assertNull(request.getHeader("Cookie"));

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, not(allOf(
                    containsString("Get session ID: NABLARCH_SID = nablarch_sid"),
                    containsString("Set session ID: NABLARCH_SID = nablarch_sid"))));
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

            NablarchSIDManager sut = new NablarchSIDManager();
            sut.processResponse(new MockHttpRequest(), new HttpResponse());

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, not(containsString("NABLARCH_SID")));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testEmptyCookie() throws UnsupportedEncodingException {
        HttpResponse response = new HttpResponse();
        response.setHeader("Set-Cookie", "");

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            NablarchSIDManager sut = new NablarchSIDManager();
            sut.processResponse(new MockHttpRequest(), response);

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, not(containsString("NABLARCH_SID")));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testHasNotSID() throws UnsupportedEncodingException {
        HttpResponse response = new HttpResponse();
        response.setHeader("Set-Cookie", "cookie=abc");

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            NablarchSIDManager sut = new NablarchSIDManager();
            sut.processResponse(new MockHttpRequest(), response);

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText,
                    containsString("Set-Cookie header value does not contain NABLARCH_SID. header value = cookie=abc"));
        } finally {
            System.setOut(originalStdOut);
        }
    }

    @Test
    public void testChangeCookieName() throws UnsupportedEncodingException {
        HttpRequest request = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
        HttpResponse response = new HttpResponse();
        response.setHeader("Set-Cookie", "ANOTHER_SID=nablarch_sid");

        PrintStream originalStdOut = System.out;
        try {
            ByteArrayOutputStream onMemoryOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(onMemoryOut, true, "UTF-8"));

            NablarchSIDManager sut = new NablarchSIDManager();
            sut.setCookieName("ANOTHER_SID");
            sut.processResponse(new MockHttpRequest(), response);
            sut.processRequest(request);
            assertThat(request.getHeader("Cookie"), is("ANOTHER_SID=nablarch_sid"));

            String logText = new String(onMemoryOut.toByteArray(), Charset.forName("UTF-8"));
            assertThat(logText, allOf(
                    containsString("Get session ID: ANOTHER_SID = nablarch_sid"),
                    containsString("Set session ID: ANOTHER_SID = nablarch_sid")));
        } finally {
            System.setOut(originalStdOut);
        }
    }
}