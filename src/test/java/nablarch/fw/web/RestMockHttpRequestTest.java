package nablarch.fw.web;

import nablarch.fw.test.MockConverter;
import nablarch.fw.test.NoContentConvertibleMockConverter;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
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
    /** リクエストパラメータ付きのGETリクエスト */
    private static final String GET_REQUEST_WITH_QUERY_AND_HOST = "GET /anotherPath?name=%E3%83%86%E3%82%B9%E3%83%88&value=%E3%82%B2%E3%83%83%E3%83%88%E3%83%AA%E3%82%AF%E3%82%A8%E3%82%B9%E3%83%88 HTTP/0.9" + LS
            + "Host: localhost" + LS
            + LS;
    /** 同名のリクエストパラメータを複数持つGETリクエスト */
    private static final String GET_REQUEST_WITH_QUERY_DUPLICATED_PARAM = "GET /test?name=%E3%83%86%E3%82%B9%E3%83%88&name=%E3%82%B2%E3%83%83%E3%83%88%E3%83%AA%E3%82%AF%E3%82%A8%E3%82%B9%E3%83%88 HTTP/1.1" + LS
            + LS;
    /** JSONをボディにもつPOSTリクエスト */
    private static final String POST_JSON_REQUEST = "POST /test HTTP/1.1" + LS
            + "Cookie: cookie=dummy" + LS
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
     * over rideしたメソッドの戻り型が{@link RestMockHttpRequest}であることを確認する。
     */
    @Test
    public void testNormalGet2() {
        RestMockHttpRequest sut = new RestMockHttpRequest(Collections.singletonList(new MockConverter())
                , "testType")
                .setHttpVersion("HTTP/0.9")
                .setHost("localhost")
                .setRequestUri("/test?name=テスト&value=ゲットリクエスト")
                .setRequestPath("/anotherPath");
        assertThat(sut.toString(), is(GET_REQUEST_WITH_QUERY_AND_HOST));
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
        assertThat(sut.getHeader("Content-Type"), is("testType"));
        assertThat((String) sut.getBody(), is("{\"field\" : \"value\"}"));

        sut.setContentType("application/json");
        assertThat(sut.getHeader("Content-Type"), is("application/json"));

        sut.setMethod("POST")
                .setRequestUri("/test")
                .setHeader("test", "OK")
                .setHeader("Content-Length", "19")
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

    /**
     * POSTリクエスト作成テスト
     * setBodyより前にsetContentTypeする
     */
    @Test
    public void testNormalPost3() {
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "testType");
        sut.setContentType("application/json")
                .setBody("{\"field\" : \"value\"}")
                .setMethod("POST")
                .setRequestUri("/test")
                .setHeader("test", "OK")
                .setHeader("Content-Length", "19")
                .setCookie(MockHttpCookie.valueOf("cookie=dummy"));
        assertThat(sut.toString(), is(POST_JSON_REQUEST));
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
     * 指定したContent-Typeを書き出し可能な{@link RestTestBodyConverter}がない場合、例外が送出されることを確認する。
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
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("url encoding failed. cause[invalid format: uri]");
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(URISyntaxException.class));
        RestMockHttpRequest sut = new RestMockHttpRequest(
                Collections.singletonList(new MockConverter()), "text/plain")
                .setRequestUri("/test");
        
        try (final MockedConstruction<URI> mocked = Mockito.mockConstructionWithAnswer(URI.class, invocation -> {
            throw new URISyntaxException("uri", "invalid format");
        })) {
            String request = sut.toString();
            fail("ここには到達しない。" + request);
        }
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
}