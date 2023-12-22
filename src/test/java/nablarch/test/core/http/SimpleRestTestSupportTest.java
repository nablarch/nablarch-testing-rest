package nablarch.test.core.http;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.StringUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.RestMockHttpRequest;
import nablarch.fw.web.RestMockHttpRequestBuilder;
import nablarch.test.RepositoryInitializer;
import nablarch.test.core.rule.TestDescription;
import nablarch.test.support.reflection.ReflectionUtil;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mockStatic;

@RunWith(Enclosed.class)
public class SimpleRestTestSupportTest {
    /**
     * {@link RestTestSupport}を継承したクラスのテスト。
     */
    public static class SimpleRestTestSupportSubClassTest extends SimpleRestTestSupport {
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        /**
         * 通常のテスト方法。
         * {@link nablarch.fw.web.RestMockHttpRequest}を作成し
         * {@link RestTestSupport#sendRequest(HttpRequest)}で内蔵サーバに送り
         * {@link HttpResponse}を受け取る。
         * 受け取ったレスポンスのステータスコードとボディを検証する。
         */
        @Test
        public void testNormal() {
            HttpResponse response = sendRequest(get("/test"));
            assertStatusCode("200 OK", HttpResponse.Status.OK, response);
            assertThat(response.getContentLength(), is("0"));
            assertNull(response.getContentType());
            assertThat(response.getCharset(), is(Charset.forName("UTF-8")));
            assertTrue(StringUtil.isNullOrEmpty(response.getBodyString()));
        }

        @Test
        public void testNormalWithSIDManager() {
            RestMockHttpRequest request = get("/test");
            ExecutionContext context = new ExecutionContext();

            sendRequestWithContext(request, context);
            assertNull(request.getHeader("Cookie"));

            sendRequestWithContext(request, context);
            assertThat(request.getHeader("Cookie"), is("NABLARCH_SID=XXXXXX"));
        }

        /**
         * SystemRepositoryにリクエストビルダーが登録されていない場合、例外が送出されることを確認する。
         */
        @Test
        public void testGetHttpRequestBuilder_ComponentNotRegistered() {
            expectedException.expect(IllegalConfigurationException.class);
            expectedException.expectMessage(
                    "could not find component. name=[restMockHttpRequestBuilder]");
            try (final MockedStatic<SystemRepository> mocked = mockStatic(SystemRepository.class)) {
                mocked.when(() -> SystemRepository.get("restMockHttpRequestBuilder")).thenReturn(null);
                getHttpRequestBuilder();
            }
            fail("ここに到達したらExceptionが発生していない");
        }

        /**
         * 存在しないテキストファイルを読み込もうとした場合、例外が送出されることを確認する。
         */
        @Test
        public void testReadTextResource_NotExistsText() {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage("couldn't find resource [SimpleRestTestSupportSubClassTest/noFile].");
            readTextResource("noFile");
        }

        /**
         * テストクラスを指定するreadTextResourcesで、
         * 存在しないテキストファイルを読み込もうとした場合、例外が送出されることを確認する。
         */
        @Test
        public void testReadTextResourceWithTestClass_NotExistsText() {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage("couldn't find resource [SimpleRestTestSupportSubClassTest/noFile].");
            readTextResource(SimpleRestTestSupportSubClassTest.class, "noFile");
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
            RestMockHttpRequest getReq = get("test");
            assertThat(getReq.getMethod(), is("GET"));
            assertThat(getReq.getRequestUri(), is("test"));

            RestMockHttpRequest postReq = post("test");
            assertThat(postReq.getMethod(), is("POST"));
            assertThat(getReq.getRequestUri(), is("test"));

            RestMockHttpRequest putReq = put("test");
            assertThat(putReq.getMethod(), is("PUT"));
            assertThat(putReq.getRequestUri(), is("test"));

            RestMockHttpRequest deleteReq = delete("test");
            assertThat(deleteReq.getMethod(), is("DELETE"));
            assertThat(deleteReq.getRequestUri(), is("test"));

            RestMockHttpRequest patchReq = patch("test");
            assertThat(patchReq.getMethod(), is("PATCH"));
            assertThat(patchReq.getRequestUri(), is("test"));

            RestMockHttpRequest headReq = newRequest("HEAD", "test");
            assertThat(headReq.getMethod(), is("HEAD"));
            assertThat(headReq.getRequestUri(), is("test"));
        }
    }

    /**
     * {@link RestTestSupport}単体でのテスト
     */
    public static class RestTestSupportInstanceTest {
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        /**
         * SystemRepositoryに{@link nablarch.fw.web.HttpServerFactory}が登録されていない場合、例外が送出されることを確認する。
         */
        @Test
        public void testSetUp_HttpServerFactoryNotRegistered() {
            expectedException.expect(IllegalConfigurationException.class);
            expectedException.expectMessage("could not find component. name=[httpServerFactory].");
            SimpleRestTestSupport sut = new SimpleRestTestSupport();
            setDummyDescription(Object.class, sut);
            RepositoryInitializer.recreateRepository("nablarch/test/core/http/no-http-server-factory.xml");
            try {
                SimpleRestTestSupport.resetHttpServer();
                sut.setUp();
            } finally {
                RepositoryInitializer.revertDefaultRepository();
            }
            fail("ここに到達したらExceptionが発生していない");
        }

        /**
         * テキストファイル読込中に{@link IOException}が送出された場合、{@link IllegalArgumentException}が送出されることを確認する。
         */
        @Test
        public void testReadTextResource_thrownIOException() {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage("couldn't read resource [response.txt]. cause [I/O error].");
            SimpleRestTestSupport sut = new SimpleRestTestSupport() {
                @Override
                protected String read(File file) throws IOException {
                    throw new IOException("I/O error");
                }
            };
            setDummyDescription(SimpleRestTestSupportTest.class, sut);
            sut.readTextResource("response.txt");
            fail("ここに到達したらExceptionが発生していない");
        }

        /**
         * テキストファイルを読み込めることを確認する。
         */
        @Test
        public void testReadTextResource() {
            SimpleRestTestSupport sut = new SimpleRestTestSupport();
            setDummyDescription(SimpleRestTestSupportTest.class, sut);
            assertThat(sut.readTextResource("response.txt"), is("HTTP/1.1 200 OK"));
        }

        /**
         * テストクラスを指定するreadTextResourceで、
         * テキストファイル読込中に{@link IOException}が送出された場合、{@link IllegalArgumentException}が送出されることを確認する。
         */
        @Test
        public void testReadTextResourceWithTestClass_thrownIOException() {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage("couldn't read resource [response.txt]. cause [I/O error].");
            SimpleRestTestSupport sut = new SimpleRestTestSupport() {
                @Override
                protected String read(File file) throws IOException {
                    throw new IOException("I/O error");
                }
            };
            sut.readTextResource(SimpleRestTestSupportTest.class, "response.txt");
            fail("ここに到達したらExceptionが発生していない");
        }

        /**
         * テストクラスを指定するreadTextResourceで、テキストファイルを読み込めることを確認する。
         */
        @Test
        public void testReadTextResourceWithTestClass() {
            SimpleRestTestSupport sut = new SimpleRestTestSupport();
            assertThat(sut.readTextResource(SimpleRestTestSupportTest.class, "response.txt"), is("HTTP/1.1 200 OK"));
        }

        /**
         * SystemRepositoryにdefaultProcessorが登録されていない場合、何もしないprocessorが設定されることを確認する。
         */
        @Test
        public void testSetUp_DefaultProcessorNotRegistered() {
            SimpleRestTestSupport sut = new SimpleRestTestSupport();
            setDummyDescription(Object.class, sut);
            RepositoryInitializer.recreateRepository("nablarch/test/core/http/no-default-processor.xml");
            try {
                sut.setUp();
                RestMockHttpRequest request = sut.get("/test");
                HttpResponse response = sut.sendRequest(request);
                assertNotNull(response.getHeader("Set-Cookie"));
                sut.sendRequest(request);
                assertNull(request.getHeader("Cookie"));
            } finally {
                RepositoryInitializer.revertDefaultRepository();
            }
        }

        /**
         * staticなHttpServerを初期化する。
         */
        @After
        public void resetHttpServer() {
            SimpleRestTestSupport.resetHttpServer();
        }

        /**
         * {@link TestDescription}に引数で渡されたクラスを設定する。
         *
         * @param clazz {@link TestDescription}に設定するクラス
         * @param sut   テスト対象の{@link RestTestSupport}
         */
        private void setDummyDescription(final Class clazz, SimpleRestTestSupport sut) {
            TestDescription description = new TestDescription() {
                {
                    this.starting(Description.createTestDescription(clazz, "dummy", new Annotation[0]));
                }
            };
            ReflectionUtil.setFieldValue(sut, "testDescription", description);
        }
    }
}
