package nablarch.test.core.http;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.RestMockHttpRequest;
import nablarch.fw.web.RestMockHttpRequestBuilder;
import nablarch.test.RepositoryInitializer;
import nablarch.test.core.db.DbAccessTestSupport;
import nablarch.test.core.rule.TestDescription;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link RestTestSupport}のテストクラス。
 */
@RunWith(Enclosed.class)
public class RestTestSupportTest {
    /**
     * {@link RestTestSupport}を継承したクラスのテスト。
     */
    public static class RestTestSupportSubClassTest extends RestTestSupport {
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
            String sessionIdReplacedResponse = response.toString().replaceAll("JSESSIONID=.+;", "JSESSIONID=DUMMY;");
            assertThat(readTextResource("response.txt"), is(sessionIdReplacedResponse));
        }

        /**
         * SystemRepositoryにリクエストビルダーが登録されていない場合、例外が送出されることを確認する。
         *
         * @param repository モック化されたリポジトリ
         */
        @Test
        public void testGetHttpRequestBuilder_ComponentNotRegistered(@Mocked final SystemRepository repository) {
            expectedException.expect(IllegalConfigurationException.class);
            expectedException.expectMessage(
                    "could not find component. name=[restMockHttpRequestBuilder]");
            new Expectations() {{
                SystemRepository.get("restMockHttpRequestBuilder");
                result = null;
            }};
            getHttpRequestBuilder();
            fail("ここに到達したらExceptionが発生していない");
        }

        /**
         * テキストファイルを読み込む際に{@link URISyntaxException}が送出された場合、例外が送出されることを確認する。
         *
         * @param url モック化されたURL
         */
        @Test
        public void testReadTextResource_CatchURISyntaxException(@Mocked final URL url) throws URISyntaxException {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage("couldn't read resource [response.txt]. cause [url is invalid: dummy].");
            new Expectations() {{
                url.toURI();
                result = new URISyntaxException("dummy", "url is invalid");
            }};
            readTextResource("response.txt");
        }

        /**
         * 存在しないテキストファイルを読み込もうとした場合、例外が送出されることを確認する。
         */
        @Test
        public void testReadTextResource_NotExistsText() {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage("couldn't find resource [RestTestSupportSubClassTest/noFile].");
            readTextResource("noFile");
        }

        /**
         * {@link DbAccessTestSupport}への委譲メソッドを確認する。
         *
         * @param dbSupport モック化されたDBテストサポート
         */
        @Test
        public void testDelegateMethod(@Mocked final DbAccessTestSupport dbSupport) {
            new Expectations() {{
                dbSupport.setUpDb("sheet");
                times = 1;
                dbSupport.setUpDb("sheet", "group");
                times = 1;
                dbSupport.getListMap("sheet", "id");
                times = 1;
                dbSupport.getListParamMap("sheet", "id");
                times = 1;
                dbSupport.getParamMap("sheet", "id");
                times = 1;
                dbSupport.assertTableEquals("sheet");
                times = 1;
                dbSupport.assertTableEquals("sheet", "id");
                times = 1;
                dbSupport.assertTableEquals("message", "sheet", "id");
                times = 1;
                dbSupport.assertTableEquals("message", "sheet", "id", true);
                times = 1;
            }};
            setUpDb("sheet");
            setUpDb("sheet", "group");
            getListMap("sheet", "id");
            getListParamMap("sheet", "id");
            getParamMap("sheet", "id");
            assertTableEquals("sheet");
            assertTableEquals("sheet", "id");
            assertTableEquals("message", "sheet", "id");
            assertTableEquals("message", "sheet", "id", true);
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
        }
    }

    /**
     * {@link RestTestSupport}単体でのテスト
     */
    public static class RestTestSupportInstanceTest {
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        /**
         * 拡張子XLS形式のExcelファイルを読み込めることを確認する。
         */
        @Test
        public void testSetUp_XlsTestData() {
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupport.class, sut);
            try {
                sut.setUp();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        /**
         * テストデータのExcelファイルが存在しない場合、処理をスキップするのみで
         * 例外が送出されないことを確認する。
         */
        @Test
        public void testSetUp_NotExistsTestData() {
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupportInstanceTest.class, sut);
            try {
                sut.setUp();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        /**
         * {@link WorkbookFactory}が例外を送出した場合、{@link RuntimeException}が送出されることを確認する。
         *
         * @param factory モック化された{@link WorkbookFactory}
         */
        @Test
        public void testSetUp_WorkbookFactoryThrowsException(@Mocked final WorkbookFactory factory) throws IOException, InvalidFormatException {
            expectedException.expect(RuntimeException.class);
            expectedException.expectMessage("test data file open failed.");
            new Expectations() {{
                WorkbookFactory.create((InputStream) any);
                result = new Exception("cannot create.");
            }};
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupport.class, sut);
            sut.setUp();
        }

        /**
         * SystemRepositoryに{@link nablarch.test.core.reader.TestDataParser}が登録されていない場合、例外が送出されることを確認する。
         *
         * @param repository モック化されたリポジトリ
         */
        @Test
        public void testGetTestDataParser_ComponentNotRegistered(@Mocked final SystemRepository repository) {
            expectedException.expect(IllegalConfigurationException.class);
            expectedException.expectMessage("could not find component. name=[testDataParser].");
            new Expectations() {{
                SystemRepository.get("testDataParser");
                result = null;
            }};
            RestTestSupport sut = new RestTestSupport();
            sut.getTestDataParser();
            fail("ここに到達したらExceptionが発生していない");
        }

        /**
         * SystemRepositoryに{@link nablarch.fw.web.HttpServerFactory}が登録されていない場合、例外が送出されることを確認する。
         */
        @Test
        public void testSetUp_HttpServerFactoryNotRegistered() {
            expectedException.expect(IllegalConfigurationException.class);
            expectedException.expectMessage("could not find component. name=[httpServerFactory].");
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(Object.class, sut);
            RepositoryInitializer.recreateRepository("nablarch/test/core/http/no-http-server-factory.xml");
            try {
                RestTestSupport.resetHttpServer();
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
            RestTestSupport sut = new RestTestSupport() {
                @Override
                protected String read(File file) throws IOException {
                    throw new IOException("I/O error");
                }
            };
            setDummyDescription(RestTestSupportTest.class, sut);
            sut.readTextResource("response.txt");
            fail("ここに到達したらExceptionが発生していない");
        }

        /**
         * staticなHttpServerを初期化する。
         */
        @After
        public void resetHttpServer() {
            RestTestSupport.resetHttpServer();
        }

        /**
         * {@link TestDescription}に引数で渡されたクラスを設定する。
         *
         * @param clazz {@link TestDescription}に設定するクラス
         * @param sut   テスト対象の{@link RestTestSupport}
         */
        private void setDummyDescription(final Class clazz, RestTestSupport sut) {
            TestDescription description = new TestDescription() {
                {
                    this.starting(Description.createTestDescription(clazz, "dummy", new Annotation[0]));
                }
            };
            Deencapsulation.setField(sut, "testDescription", description);
        }
    }

}
