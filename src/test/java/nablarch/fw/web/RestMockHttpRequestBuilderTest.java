package nablarch.fw.web;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static nablarch.test.Assertion.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * {@link RestMockHttpRequestBuilder}のテストクラス。
 */
public class RestMockHttpRequestBuilderTest {
    /** テスト対象 */
    RestMockHttpRequestBuilder sut = new RestMockHttpRequestBuilder();

    /**
     * 利用可能な{@link BodyConverter}のテスト。
     * デフォルトでは{@link StringBodyConverter}と{@link JacksonBodyConverter}を持つこと
     * {@link RestMockHttpRequestBuilder#setBodyConverters(Collection)}で
     * bodyConvertersが設定できることを確認する。
     */
    @Test
    public void testBodyConverter() throws NoSuchFieldException, IllegalAccessException {
        Field convertersField = sut.getClass().getDeclaredField("bodyConverters");
        convertersField.setAccessible(true);
        List<BodyConverter> defaultList = (List<BodyConverter>) convertersField.get(sut);
        assertEquals(2, defaultList.size());
        for (BodyConverter converter : defaultList) {
            if (!(converter instanceof StringBodyConverter)
                    && !(converter instanceof JacksonBodyConverter)) {
                fail("default BodyConverter list has unknown BodyConverter.");
            }
        }

        List<BodyConverter> bodyConverters = Arrays.asList(new StringBodyConverter(), new JacksonBodyConverter());
        sut.setBodyConverters(bodyConverters);
        List<BodyConverter> actual = (List<BodyConverter>) convertersField.get(sut);
        assertThat(actual, is(bodyConverters));
    }

    /**
     * デフォルトContent-Typeのテスト。
     * 未設定の場合デフォルトが"application/json"であること、
     * {@link RestMockHttpRequestBuilder#setDefaultContentType(String)}で
     * 設定できることを確認する。
     */
    @Test
    public void testDefaultContentType() throws NoSuchFieldException, IllegalAccessException {
        Field defaultContentTypeField = sut.getClass().getDeclaredField("defaultContentType");
        defaultContentTypeField.setAccessible(true);
        String defaultValue = (String) defaultContentTypeField.get(sut);
        assertThat(defaultValue, is("application/json"));

        sut.setDefaultContentType("text/plain");
        String textPlain = (String) defaultContentTypeField.get(sut);
        assertThat(textPlain, is("text/plain"));
    }

    /**
     * {@link RestMockHttpRequestBuilder#get(String)}
     * {@link RestMockHttpRequestBuilder#post(String)}
     * {@link RestMockHttpRequestBuilder#put(String)}
     * {@link RestMockHttpRequestBuilder#delete(String)}
     * でそれぞれに対応する{@link RestMockHttpRequest}が生成されることを確認する。
     */
    @Test
    public void newRequestTest() {
        RestMockHttpRequest getReq = sut.get("test");
        assertThat(getReq.getMethod(), is("GET"));
        assertThat(getReq.getRequestUri(), is("test"));

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