package nablarch.fw.web;

import mockit.Expectations;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
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
            + LS;
    /** リクエストURIを指定して作成されるHTTPリクエスト */
    private static final String GET_REQUEST = "GET /test HTTP/1.1" + LS
            + LS;
    /** リクエストパラメータ付きのGETリクエスト */
    private static final String GET_REQUEST_WITH_QUERY = "GET /test?name=%E3%83%86%E3%82%B9%E3%83%88&value=%E3%82%B2%E3%83%83%E3%83%88%E3%83%AA%E3%82%AF%E3%82%A8%E3%82%B9%E3%83%88 HTTP/1.1" + LS
            + LS;
    /** 同名のリクエストパラメータを複数持つGETリクエスト */
    private static final String GET_REQUEST_WITH_QUERY_DUPLICATED_PARAM = "GET /test?name=%E3%83%86%E3%82%B9%E3%83%88&name=%E3%82%B2%E3%83%83%E3%83%88%E3%83%AA%E3%82%AF%E3%82%A8%E3%82%B9%E3%83%88 HTTP/1.1" + LS
            + LS;
    /** JSONをボディにもつPOSTリクエスト */
    private static final String POST_JSON_REQUEST = "POST /test HTTP/1.1" + LS
            + "Cookie: {cookie=dummy}" + LS
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
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType");
        assertThat(sut.getMethod(), is("GET"));
        assertThat(sut.getRequestUri(), is("/"));
        assertTrue(sut.getHeaderMap().isEmpty());
        assertNull(sut.getMediaType());
        assertNull(sut.getBody());
        assertThat(sut.toString(), is(DEFAULT_REQUEST));
    }

    /**
     * GETリクエスト作成テスト。
     * リクエストURIを指定したリクエストが想定通りに生成されることを確認する。
     */
    @Test
    public void testNormalGet1() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
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
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
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
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType")
                .setRequestUri("/test?name=テスト")
                .setParam("value", "ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY));
    }

    /**
     * GETリクエスト作成テスト。
     * {@link RestMockHttpRequest#setParamMap(Map)}で
     * 設定したパラメータがURIのクエリストリングとしてリクエストが生成されることを確認する。
     */
    @Test
    public void testNormalGet4() {
        Map<String, String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put("name", new String[]{"テスト"});
        paramMap.put("value", new String[]{"ゲットリクエスト"});

        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType")
                .setRequestUri("/test")
                .setParamMap(paramMap);
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY));
    }

    /**
     * GETリクエスト作成テスト。
     * クエリストリングに同名のパラメータをもつリクエストが想定通りに生成されることを確認する。
     */
    @Test
    public void testNormalGet5() {
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType")
                .setRequestUri("/test?name=テスト&name=ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY_DUPLICATED_PARAM));
    }

    /**
     * GETリクエスト作成テスト。
     * クエリストリングと{@link RestMockHttpRequest#setParam(String, String...)}で
     * 設定したパラメータが両方クエリストリングとしてリクエストが生成されることを確認する。
     */
    @Test
    public void testNormalGet6() {
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType")
                .setRequestUri("/test?name=テスト")
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
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType")
                .setRequestUri("/test")
                .setParam("name", "テスト", "ゲットリクエスト");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY_DUPLICATED_PARAM));
    }

    /**
     * POSTリクエスト作成テスト
     * JSON形式のボディを持つリクエストが作成されることを確認する。
     */
    @Test
    public void testNormalPost1() {
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType");
        sut.setBody("{\"field\" : \"value\"}");
        assertEquals(new MediaType("testType"), sut.getMediaType());
        assertThat((String) sut.getBody(), is("{\"field\" : \"value\"}"));

        sut.setContentType("application/json");
        assertEquals(new MediaType("application/json"), sut.getMediaType());

        Map<String, String> headerMap = sut.getHeaderMap();
        headerMap.put("test", "OK");
        sut.setMethod("POST")
                .setRequestUri("/test")
                .setHeaderMap(headerMap)
                .setCookie(MockHttpCookie.valueOf("cookie=dummy"));
        assertThat(sut.toString(), is(POST_JSON_REQUEST));
    }

    /**
     * POSTリクエスト作成テスト
     * URLエンコードされたパラメータをボディに持つリクエストが作成されることを確認する。
     */
    @Test
    public void testNormalPost2() {
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType")
                .setMethod("POST")
                .setRequestUri("/test")
                .setContentType("application/x-www-form-urlencoded")
                .setParam("name", "テスト");
        assertThat(sut.toString(), is(POST_FORM_REQUEST));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * ボディがあるのにContent-Typeが指定されていない場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalRequestHasNotContentTypeWithBody() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("there was no Content-Type header but body was not empty.");
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), null)
                .setBody("test body");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * 指定したContent-Typeを書き出し可能な{@link BodyConverter}がない場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalCouldNotFindBodyConverter() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("unsupported media type requested. MIME type = [ text/plain ]");
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new NoContentConvertibleMockConverter()), "text/plain")
                .setBody("test body");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * URIのインスタンス化に失敗した場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalURIConstructorThrowsURISyntaxException() throws URISyntaxException {
        new Expectations(URI.class) {{
            new URI(anyString);
            result = new URISyntaxException("uri", "invalid format");
        }};
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("url encoding failed. cause[invalid format: uri]");
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(URISyntaxException.class));
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "text/plain")
                .setRequestUri("/test");
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
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "text/plain")
                .setRequestUri("/test")
                .setParam("name", "テスト");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * リクエストパラメータとボディが同時に設定された場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalParamMapAndBodyAreNotBothEmpty() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("set only one of paramMap or body.");
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "text/plain")
                .setRequestUri("/test")
                .setBody("test body")
                .setParam("parameter", "value");
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * 誤ったContent-Lengthが設定された場合、例外が送出されることを確認する。
     */
    @Test
    public void testAbnormalWrongContentLengthSet() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("wrong Content-Length[1] was set.correct length is [19].");
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType");
        Map<String, String> headerMap = sut.getHeaderMap();
        headerMap.put("Content-Length", "1");
        sut.setBody("{\"field\" : \"value\"}")
                .setHeaderMap(headerMap);
        String request = sut.toString();
        fail("ここには到達しない。" + request);
    }

    /**
     * テスト用の{@link BodyConverter}実装。
     * Content-Typeが何であってもボディをStringにキャストして返す。
     */
    private static class MockConverter implements BodyConverter {
        @Override
        public boolean isConvertible(Object body, MediaType mediaType) {
            return true;
        }

        @Override
        public String convert(Object body, MediaType mediaType) {
            return (String) body;
        }
    }

    /**
     * テスト用の{@link BodyConverter}実装。
     * {@link BodyConverter#isConvertible(Object, MediaType)}で常にfalseを返す。
     */
    private static class NoContentConvertibleMockConverter implements BodyConverter {

        @Override
        public boolean isConvertible(Object body, MediaType mediaType) {
            return false;
        }

        @Override
        public String convert(Object body, MediaType mediaType) {
            return null;
        }
    }
}