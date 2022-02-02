package nablarch.test.core.http;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.test.RepositoryInitializer;
import nablarch.test.TestSupport;
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

            DbAccessTestSupport dbSupport = Deencapsulation.getField(sut, "dbSupport");
            TestSupport testSupport = Deencapsulation.getField(dbSupport, "testSupport");
            Object testClass = Deencapsulation.getField(testSupport, "testClass");

            assertThat(testClass, is((Object)RestTestSupportInstanceTest.class));
        }

        /**
         * デフォルトコンストラクタでインスタンスを生成した場合、
         * RestTestSupportのクラスオブジェクトが DbAccessTestSupport に渡されて初期化されることを確認する。
         */
        @Test
        public void testDefaultConstructor() {
            RestTestSupport sut = new RestTestSupport();

            DbAccessTestSupport dbSupport = Deencapsulation.getField(sut, "dbSupport");
            TestSupport testSupport = Deencapsulation.getField(dbSupport, "testSupport");
            Object testClass = Deencapsulation.getField(testSupport, "testClass");

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
            sut.setUpDb();
            fail("ここに到達したらExceptionが発生していない");
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
