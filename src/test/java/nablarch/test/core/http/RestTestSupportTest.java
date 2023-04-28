package nablarch.test.core.http;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.web.HttpResponse;
import nablarch.test.RepositoryInitializer;
import nablarch.test.TestSupport;
import nablarch.test.core.db.DbAccessTestSupport;
import nablarch.test.core.rule.TestDescription;
import nablarch.test.support.reflection.ReflectionUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * {@link RestTestSupport}のテストクラス。
 */
@RunWith(Enclosed.class)
public class RestTestSupportTest {
    /**
     * {@link RestTestSupport}を継承したクラスのテスト。
     */
    public static class RestTestSupportSubClassTest extends RestTestSupport {
        
        /**
         * {@link DbAccessTestSupport}への委譲メソッドを確認する。
         */
        @Test
        public void testDelegateMethod() {
            // dbSupport は親クラス(RestTestSupport)のインスタンス初期化時に設定されるため、
            // サブクラスで mockConstruction を使っても間に合わない。
            // このため、インスタンスを取り出して spy に置き換えている。
            // なお、通常の spy だと Excel に実際にアクセスしようとしてエラーになるので、
            // 動きをモック化させるため defaultAnswer(RETURNS_DEFAULTS) を設定している。
            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(this, "dbSupport");
            final DbAccessTestSupport spiedDbSupport = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(this, "dbSupport", spiedDbSupport);

            setUpDb("sheet");
            setUpDb("sheet", "group");
            getListMap("sheet", "id");
            getListParamMap("sheet", "id");
            getParamMap("sheet", "id");
            assertTableEquals("sheet");
            assertTableEquals("sheet", "id");
            assertTableEquals("message", "sheet", "id");
            assertTableEquals("message", "sheet", "id", true);

            verify(spiedDbSupport).setUpDb("sheet");
            verify(spiedDbSupport).setUpDb("sheet", "group");
            verify(spiedDbSupport).getListMap("sheet", "id");
            verify(spiedDbSupport).getListParamMap("sheet", "id");
            verify(spiedDbSupport).getParamMap("sheet", "id");
            verify(spiedDbSupport).assertTableEquals("sheet");
            verify(spiedDbSupport).assertTableEquals("sheet", "id");
            verify(spiedDbSupport).assertTableEquals("message", "sheet", "id");
            verify(spiedDbSupport).assertTableEquals("message", "sheet", "id", true);
        }
    }

    /**
     * {@link RestTestSupport}単体でのテスト
     */
    public static class RestTestSupportInstanceTest {
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        /**
         * テストクラスを指定するコンストラクタでインスタンスを生成した場合、
         * DbAccessTestSupport にテストクラスが渡されて初期化されることを確認する。
         */
        @Test
        public void testConstructorWithTestClass() {
            RestTestSupport sut = new RestTestSupport(RestTestSupportInstanceTest.class);

            DbAccessTestSupport dbSupport = ReflectionUtil.getFieldValue(sut, "dbSupport");
            TestSupport testSupport = ReflectionUtil.getFieldValue(dbSupport, "testSupport");
            Object testClass = ReflectionUtil.getFieldValue(testSupport, "testClass");

            assertThat(testClass, is((Object)RestTestSupportInstanceTest.class));
        }

        /**
         * デフォルトコンストラクタでインスタンスを生成した場合、
         * RestTestSupportのクラスオブジェクトが DbAccessTestSupport に渡されて初期化されることを確認する。
         */
        @Test
        public void testDefaultConstructor() {
            RestTestSupport sut = new RestTestSupport();

            DbAccessTestSupport dbSupport = ReflectionUtil.getFieldValue(sut, "dbSupport");
            TestSupport testSupport = ReflectionUtil.getFieldValue(dbSupport, "testSupport");
            Object testClass = ReflectionUtil.getFieldValue(testSupport, "testClass");

            assertThat(testClass, is((Object)RestTestSupport.class));
        }

        /**
         * 拡張子XLS形式のExcelファイルを読み込めることを確認する。
         */
        @Test
        public void testSetUp_XlsTestData() {
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupport.class, sut);
            try {
                sut.setUpDb();
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
                sut.setUpDb();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        /**
         * SystemRepositoryにnablarch.test.resource-rootが登録されていない場合、
         * デフォルト値が採用されエラーとならないことを確認する。
         */
        @Test
        public void testSetUp_resourceRootIsNotSet() {
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupport.class, sut);
            RepositoryInitializer.recreateRepository("nablarch/test/core/http/no-resource-root.xml");
            try {
                RestTestSupport.resetHttpServer();
                try {
                    sut.setUpDb();
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            } finally {
                RepositoryInitializer.revertDefaultRepository();
            }
        }

        /**
         * {@link WorkbookFactory}が例外を送出した場合、{@link RuntimeException}が送出されることを確認する。
         */
        @Test
        public void testSetUp_WorkbookFactoryThrowsException() throws IOException, InvalidFormatException {
            expectedException.expect(RuntimeException.class);
            expectedException.expectMessage("test data file open failed.");
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupport.class, sut);
            try (final MockedStatic<WorkbookFactory> mocked = mockStatic(WorkbookFactory.class)) {
                mocked.when(() -> WorkbookFactory.create(any(InputStream.class)))
                        .thenThrow(new IOException("cannot create."));
                sut.setUpDb();
            }
            fail("ここに到達したらExceptionが発生していない");
        }

        /**
         * SystemRepositoryに{@link nablarch.test.core.reader.TestDataParser}が登録されていない場合、例外が送出されることを確認する。
         */
        @Test
        public void testGetTestDataParser_ComponentNotRegistered() {
            expectedException.expect(IllegalConfigurationException.class);
            expectedException.expectMessage("could not find component. name=[testDataParser].");
            try (final MockedStatic<SystemRepository> mocked = mockStatic(SystemRepository.class)) {
                mocked.when(() -> SystemRepository.get("testDataParser")).thenReturn(null);
                
                RestTestSupport sut = new RestTestSupport();
                sut.getTestDataParser();
            }
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
            ReflectionUtil.setFieldValue(sut, "testDescription", description);
        }

        /**
         * レスポンス内容の文字列比較。
         */
        @Test
        public void testWritingToBodyBuffer() {
            HttpResponse res = new HttpResponse();
            res.setContentType("text/plain ; charset= \"utf-8\" ");
            assertThat("0", is(res.getContentLength()));
            
            res.write("Hello world!\nボディテスト\n");

            RestTestSupport sut = new RestTestSupport();
            // レスポンスボディ確認
            assertThat("Hello world!\nボディテスト\n", is(sut.getBodyString(res)));
        }

        /**
         * ストリームにbyte配列を書き出し、toStringで内容を確認する。
         */
        @Test
        public void testWritingToBodyOutputStream() throws Exception {
            HttpResponse res = new HttpResponse();
            String expectedString = "Hello world!\n" + "Hello world2!\n" + "Hello world3!\nボディテスト\n";
            byte[] expectedBytes = expectedString.getBytes(Charset.forName("UTF-8"));

            res.write(expectedBytes);

            byte[] actualBytes = new byte[expectedBytes.length];
            RestTestSupport sut = new RestTestSupport();
            InputStream input = sut.getBodyStream(res);
            input.read(actualBytes);

            assertThat(actualBytes, is(expectedBytes));
        }
    }
}
