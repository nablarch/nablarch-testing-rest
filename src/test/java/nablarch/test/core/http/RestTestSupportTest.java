package nablarch.test.core.http;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.web.HttpResponse;
import nablarch.test.RepositoryInitializer;
import nablarch.test.TestSupport;
import nablarch.test.core.db.DbAccessTestSupport;
import nablarch.test.core.reader.TestDataParser;
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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
         * .xlsxファイルが存在しシートが見つかる場合、{@code setUpDb}が呼ばれることを確認する。
         */
        @Test
        public void testSetUpDbIfSheetExists_XlsxFileSheetFound() {
            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(this, "dbSupport");
            final DbAccessTestSupport spy = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(this, "dbSupport", spy);

            setUpDbIfSheetExists("setUpDb");

            verify(spy).setUpDb("setUpDb");
        }

        /**
         * .xlsxファイルが存在するがシートが存在しない場合、{@code setUpDb}が呼ばれるが例外が送出されないことを確認する。
         * ファイル単位で存在確認するため、シートが存在しなくても{@code setUpDb}は呼ばれる。
         * {@code dbSupport.setUpDb()}はシートが存在しない場合、空リストを返して自然にスキップする。
         */
        @Test
        public void testSetUpDbIfSheetExists_XlsxFileSheetNotFound_callsSetUpDb() {
            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(this, "dbSupport");
            final DbAccessTestSupport spy = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(this, "dbSupport", spy);

            setUpDbIfSheetExists("nonExistentSheet");

            verify(spy).setUpDb("nonExistentSheet");
        }

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
        /** テストデータの実際の場所（{@code nablarch.test.resource-root} を stub するときに使う） */
        private static final String RESOURCE_ROOT = "src/test/java";

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
         * .xlsファイルが存在しシートが見つかる場合、{@code setUpDb}が呼ばれることを確認する。
         */
        @Test
        public void testSetUpDbIfSheetExists_XlsFileSheetFound() {
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupport.class, sut);

            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(sut, "dbSupport");
            final DbAccessTestSupport spy = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(sut, "dbSupport", spy);

            sut.setUpDbIfSheetExists("setUpDb");

            verify(spy).setUpDb("setUpDb");
        }

        /**
         * テストデータファイルが存在しない場合、{@code setUpDb}が呼ばれないことを確認する。
         */
        @Test
        public void testSetUpDbIfSheetExists_NoFile() {
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupportInstanceTest.class, sut);

            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(sut, "dbSupport");
            final DbAccessTestSupport spy = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(sut, "dbSupport", spy);

            sut.setUpDbIfSheetExists("setUpDb");

            verify(spy, never()).setUpDb(any());
        }

        /**
         * .xlsファイルが存在するがシートが存在しない場合、{@code setUpDb}が呼ばれるが例外が送出されないことを確認する。
         * ファイル単位で存在確認するため、シートが存在しなくても{@code setUpDb}は呼ばれる。
         * {@code dbSupport.setUpDb()}はシートが存在しない場合、空リストを返して自然にスキップする。
         */
        @Test
        public void testSetUpDbIfSheetExists_XlsFileSheetNotFound_callsSetUpDb() {
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupport.class, sut);

            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(sut, "dbSupport");
            final DbAccessTestSupport spy = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(sut, "dbSupport", spy);

            sut.setUpDbIfSheetExists("nonExistentSheet");

            verify(spy).setUpDb("nonExistentSheet");
        }

        /**
         * Excel ファイルが存在しないクラスでも、差し替えた {@code testDataParser} の
         * {@code isResourceExisting()} が true を返せば {@code setUpDb} が呼ばれることを確認する。
         */
        @Test
        public void testSetUpDbIfSheetExists_testDataParserReturnsExisting_callsSetUpDb() {
            RestTestSupport sut = new RestTestSupport();
            // Excel ファイルが存在しないクラスを設定する
            setDummyDescription(RestTestSupportInstanceTest.class, sut);
            assertFalse(new File(RESOURCE_ROOT, "nablarch/test/core/http/RestTestSupportInstanceTest.xls").exists());
            assertFalse(new File(RESOURCE_ROOT, "nablarch/test/core/http/RestTestSupportInstanceTest.xlsx").exists());

            // testDataParser を isResourceExisting が常に true を返す mock に差し替え
            TestDataParser mockParser = mock(TestDataParser.class);
            when(mockParser.isResourceExisting(any(), any())).thenReturn(true);

            // dbSupport を spy に差し替え
            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(sut, "dbSupport");
            final DbAccessTestSupport spy = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(sut, "dbSupport", spy);

            // SystemRepository をモック化するとリソースルート設定が失われ既定値（test/java/）に落ちるため、
            // テストデータの実際の場所を単一パスで stub する（ディスク上のファイルの有無で判定する退行を検知できるようにする）
            try (MockedStatic<SystemRepository> mocked = mockStatic(SystemRepository.class)) {
                mocked.when(() -> SystemRepository.get("nablarch.test.resource-root")).thenReturn(RESOURCE_ROOT);
                mocked.when(() -> SystemRepository.get("testDataParser")).thenReturn(mockParser);

                sut.setUpDbIfSheetExists("setUpDb");
            }

            verify(spy).setUpDb("setUpDb");
        }

        /**
         * Excel ファイルが存在するクラスでも、差し替えた {@code testDataParser} の
         * {@code isResourceExisting()} が false を返せば {@code setUpDb} が呼ばれないことを確認する。
         * ディスク上のファイルの有無ではなく、{@code testDataParser} の判定だけで決まる。
         */
        @Test
        public void testSetUpDbIfSheetExists_testDataParserReturnsNotExisting_skipsSetUpDb() {
            RestTestSupport sut = new RestTestSupport();
            // Excel ファイル（RestTestSupport.xls）が存在するクラスを設定する
            setDummyDescription(RestTestSupport.class, sut);
            assertTrue(new File(RESOURCE_ROOT, "nablarch/test/core/http/RestTestSupport.xls").isFile());

            TestDataParser mockParser = mock(TestDataParser.class);
            when(mockParser.isResourceExisting(any(), any())).thenReturn(false);

            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(sut, "dbSupport");
            final DbAccessTestSupport spy = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(sut, "dbSupport", spy);

            // SystemRepository をモック化するとリソースルート設定が失われ既定値（test/java/）に落ちるため、
            // テストデータの実際の場所を単一パスで stub する（ディスク上のファイルの有無で判定する退行を検知できるようにする）
            try (MockedStatic<SystemRepository> mocked = mockStatic(SystemRepository.class)) {
                mocked.when(() -> SystemRepository.get("nablarch.test.resource-root")).thenReturn(RESOURCE_ROOT);
                mocked.when(() -> SystemRepository.get("testDataParser")).thenReturn(mockParser);

                sut.setUpDbIfSheetExists("setUpDb");
            }

            verify(spy, never()).setUpDb(any());
        }

        /**
         * 一度 {@code isResourceExisting()} が false を返すと、以降のシートでは
         * {@code testDataParser} に問い合わせず {@code setUpDb} も呼ばれないことを確認する。
         * {@code isResourceExisting()} の判定単位は {@code testDataParser} の実装に委ねられる
         * （Excel 形式はファイル単位、YAML 形式はディレクトリ単位）ため、この振る舞いは
         * 「1 つ目のシートが無いだけで 2 つ目が読まれない」ことを意味しない。
         */
        @Test
        public void testSetUpDbIfSheetExists_onceNotExisting_doesNotAskParserAgain() {
            RestTestSupport sut = new RestTestSupport();
            setDummyDescription(RestTestSupport.class, sut);

            TestDataParser mockParser = mock(TestDataParser.class);
            when(mockParser.isResourceExisting(any(), any())).thenReturn(false);

            final DbAccessTestSupport original = ReflectionUtil.getFieldValue(sut, "dbSupport");
            final DbAccessTestSupport spy = mock(DbAccessTestSupport.class,
                    withSettings().spiedInstance(original).defaultAnswer(RETURNS_DEFAULTS));
            ReflectionUtil.setFieldValue(sut, "dbSupport", spy);

            // SystemRepository をモック化するとリソースルート設定が失われ既定値（test/java/）に落ちるため、
            // テストデータの実際の場所を単一パスで stub する（ディスク上のファイルの有無で判定する退行を検知できるようにする）
            try (MockedStatic<SystemRepository> mocked = mockStatic(SystemRepository.class)) {
                mocked.when(() -> SystemRepository.get("nablarch.test.resource-root")).thenReturn(RESOURCE_ROOT);
                mocked.when(() -> SystemRepository.get("testDataParser")).thenReturn(mockParser);

                sut.setUpDbIfSheetExists("setUpDb");
                sut.setUpDbIfSheetExists("dummy");
            }

            verify(mockParser, times(1)).isResourceExisting(any(), any());
            verify(spy, never()).setUpDb(any());
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
