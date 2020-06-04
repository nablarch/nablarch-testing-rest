package nablarch.fw.web;

import mockit.Expectations;
import mockit.Mocked;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link RestMockHttpRequest}のテストクラス。
 */
public class RestMockHttpRequestTest {
    /** 改行 */
    private static final String LS = "\r\n";
    /** デフォルトで作成されるHTTPリクエスト */
    private static final String DEFAULT_REQUEST = "GET / HTTP/1.1" + LS
            + "Content-Length: 0" + LS
            + LS;
    /** リクエストURIを指定して作成されるHTTPリクエスト */
    private static final String GET_REQUEST = "GET /test HTTP/1.1" + LS
            + "Content-Length: 0" + LS
            + LS;
    /** リクエストパラメータ付きのGETリクエスト */
    private static final String GET_REQUEST_WITH_QUERY = "GET /test?name=%E3%83%86%E3%82%B9%E3%83%88&value=%E3%82%B2%E3%83%83%E3%83%88%E3%83%AA%E3%82%AF%E3%82%A8%E3%82%B9%E3%83%88 HTTP/1.1" + LS
            + "Content-Length: 0" + LS
            + LS;
    /** 同名のリクエストパラメータを複数持つGETリクエスト */
    private static final String GET_REQUEST_WITH_QUERY_DUPLICATED_PARAM = "GET /test?name=%E3%83%86%E3%82%B9%E3%83%88&name=%E3%82%B2%E3%83%83%E3%83%88%E3%83%AA%E3%82%AF%E3%82%A8%E3%82%B9%E3%83%88 HTTP/1.1" + LS
            + "Content-Length: 0" + LS
            + LS;
    /** JSONをボディにもつPOSTリクエスト */
    private static final String POST_JSON_REQUEST = "POST /test HTTP/1.1" + LS
            + "test: OK" + LS
            + "Content-Length: 19" + LS
            + "Content-Type: application/json" + LS
            + LS
            + "{\"field\" : \"value\"}";
    /** URLエンコードされたパラメータをボディにもつPOSTリクエスト */
    private static final String POST_FORM_REQUEST = "POST /test HTTP/1.1" + LS
            + "Content-Length: 32" + LS
            + "Content-Type: application/x-www-form-urlencoded" + LS
            + LS
            + "name=%E3%83%86%E3%82%B9%E3%83%88";

    /**
     * デフォルトで作成されるリクエストを確認する。
     */
    @Test
    public void testNormalDefault() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        assertThat(sut.getMethod(), is("GET"));
        assertThat(sut.getRequestUri(), is("/"));
        assertTrue(sut.getHeaderMap().isEmpty());
        assertNull(sut.getContentType());
        assertNull(sut.getBody());
        assertThat(sut.toString(), is(DEFAULT_REQUEST));
    }

    /**
     * GETリクエスト作成テスト。
     * リクエストURIを指定したリクエストが想定通りに生成されることを確認する。
     */
    @Test
    public void testNormalGet1() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setRequestUri("/test");
        assertThat(sut.toString(), is(GET_REQUEST));
    }

    /**
     * GETリクエスト作成テスト。
     * クエリストリング付きのリクエストURIを指定したリクエストが想定通りに生成されることを確認する。
     */
    @Test
    public void testNormalGet2() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setRequestUri("/test?name=テスト&value=ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY));
    }

    /**
     * GETリクエスト作成テスト。
     * クエリストリング付きのリクエストURIを指定し、かつ{@link RestMockHttpRequest#setParam(String, String...)}で
     * 設定したパラメータを持つリクエストが想定通りに生成されることを確認する。
     */
    @Test
    public void testNormalGet3() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setRequestUri("/test?name=テスト")
                .setParam("value", "ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY));
    }

    /**
     * GETリクエスト作成テスト。
     * {@link RestMockHttpRequest#setParam(String, String...)}で
     * 設定したパラメータがURIのクエリストリングとしてリクエストが生成されることを確認する。
     */
    @Test
    public void testNormalGet4() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setRequestUri("/test")
                .setParam("name", "テスト")
                .setParam("value", "ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY));
    }

    /**
     * GETリクエスト作成テスト。
     * クエリストリングに同名のパラメータをもつリクエストが想定通りに生成されることを確認する。
     */
    @Test
    public void testNormalGet5() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setRequestUri("/test?name=テスト&name=ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY_DUPLICATED_PARAM));
    }

    /**
     * GETリクエスト作成テスト。
     * クエリストリングと{@link RestMockHttpRequest#setParam(String, String...)}で
     * 設定したパラメータが両方クエリストリングとしてリクエストが生成されることを確認する。
     */
    @Test
    public void testNormalGet6() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setRequestUri("/test?name=テスト")
                .setParam("name", "ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY_DUPLICATED_PARAM));
    }

    /**
     * GETリクエスト作成テスト。
     * {@link RestMockHttpRequest#setParam(String, String...)}で
     * 複数パラメータを設定したリクエストが想定通りに生成されることを確認する。
     */
    @Test
    public void testNormalGet7() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setRequestUri("/test")
                .setParam("name", "テスト", "ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY_DUPLICATED_PARAM));
    }

    /**
     * POSTリクエスト作成テスト
     * JSON形式のボディを持つリクエストが作成されることを確認する。
     */
    @Test
    public void testNormalPost1() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setBody("{\"field\" : \"value\"}");
        assertThat(sut.getContentType(), is("testType"));
        assertThat((String) sut.getBody(), is("{\"field\" : \"value\"}"));

        sut.setContentType("application/json");
        assertThat(sut.getContentType(), is("application/json"));

        Map<String, String> headerMap = sut.getHeaderMap();
        headerMap.put("test", "OK");
        sut.setMethod("POST").setRequestUri("/test").setHeaderMap(headerMap);
        assertThat(sut.toString(), is(POST_JSON_REQUEST));
    }

    /**
     * POSTリクエスト作成テスト
     * URLエンコードされたパラメータをボディに持つリクエストが作成されることを確認する。
     */
    @Test
    public void testNormalPost2() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setMethod("POST").setRequestUri("/test")
                .setContentType("application/x-www-form-urlencoded")
                .setParam("name", "テスト");
        assertThat(sut.toString(), is(POST_FORM_REQUEST));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * クエリストリングが"name=value"形式でない場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalInvalidQueryString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("name must be name=value format.");
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter())
                , "testType");
        sut.setRequestUri("/test?name");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * ボディがあるのにContent-Typeが指定されていない場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalRequestHasNotContentTypeWithBody() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("there was no Content-Type header but body was not empty.");
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter()), null);
        sut.setBody("test body");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * 書き出しに使用する{@link HttpBodyWriter}から{@link IOException}が送出された場合、
     * 例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalWriterThrowsIOException() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("an error occurred while Writer was writing body.");
        expectedException.expectCause(CoreMatchers.<Throwable>allOf(
                instanceOf(IOException.class)
                , hasProperty("message", is("Writer"))
        ));
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new ThrowExceptionMockWriter()), null);
        sut.setBody("test body");
        sut.setContentType("text/plain");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * 指定したContent-Typeを書き出し可能な{@link HttpBodyWriter}がない場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalCouldNotFindBodyWriter() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("unsupported media type requested. Content-Type = [ text/plain ]");
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new NoContentWritableMockWriter()), "text/plain");
        sut.setBody("test body");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * URLエンコードに失敗した場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalUrlEncoderThrowsUnsupportedEncodingException() throws UnsupportedEncodingException {
        new Expectations(URLEncoder.class) {{
            URLEncoder.encode(anyString, "UTF-8");
            result = new UnsupportedEncodingException("error utf-8");
        }};
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("url encoding failed.");
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(UnsupportedEncodingException.class));
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter()), "text/plain");
        sut.setRequestUri("/test").setParam("name", "テスト");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * 内部でもつ{@link StringWriter}から{@link IOException}が送出された場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalStringWriterThrowsIOException(@Mocked final StringWriter writer) throws IOException {
        new Expectations() {{
            writer.close();
            result = new IOException("close failed.");
        }};
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("an error occurred while Writer was being closed.");
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(IOException.class));
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockWriter()), "text/plain");
        sut.setRequestUri("/test").setBody("test body");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * テスト用の{@link HttpBodyWriter}実装。
     * Content-Typeが何であっても{@link Writer#write(String)}で書き出しを行う。
     */
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

    /**
     * テスト用の{@link HttpBodyWriter}実装。
     * 常に書き出し時に{@link IOException}を投げる。
     */
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

    /**
     * テスト用の{@link HttpBodyWriter}実装。
     * {@link HttpBodyWriter#isWritable(Object, String)}で常にfalseを返す。
     */
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