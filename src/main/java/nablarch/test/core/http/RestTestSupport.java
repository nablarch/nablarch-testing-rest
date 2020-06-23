package nablarch.test.core.http;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.HttpServer;
import nablarch.fw.web.HttpServerFactory;
import nablarch.fw.web.ResourceLocator;
import nablarch.fw.web.RestMockHttpRequest;
import nablarch.fw.web.RestMockHttpRequestBuilder;
import nablarch.fw.web.servlet.WebFrontController;
import nablarch.test.NablarchTestUtils;
import nablarch.test.core.db.DbAccessTestSupport;
import nablarch.test.core.reader.TestDataParser;
import nablarch.test.core.rule.TestDescription;
import nablarch.test.event.TestEventDispatcher;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Rule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * RESTfulウェブサービス用のテストサポートクラス
 */
@Published
public class RestTestSupport extends TestEventDispatcher {
    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(RestTestSupport.class);
    /** HTTPサーバファクトリのリポジトリキー */
    private static final String HTTP_SERVER_FACTORY_KEY = "httpServerFactory";
    /** RestTestConfigurationのリポジトリキー */
    private static final String REST_TEST_CONFIGURATION_KEY = "restTestConfiguration";
    /** RestMockHttpRequestBuilderのリポジトリキー */
    private static final String HTTP_REQUEST_BUILDER_KEY = "restMockHttpRequestBuilder";
    /** TestDataParserのリポジトリキー */
    public static final String TEST_DATA_PARSER_KEY = "testDataParser";
    /** ベースディレクトリを取得するためのリポジトリキー */
    private static final String RESOURCE_ROOT_KEY = "nablarch.test.resource-root";

    /** リソース読み込み時のベースディレクトリ */
    private static final String DEFAULT_RESOURCE_ROOT = "test/java/";
    /** パスセパレータ */
    private static final String PATH_SEPARATOR = ";";

    /** テストクラス共通データを定義しているシート名 */
    private static final String SETUP_TABLE_SHEET = "setUpDb";

    /** 初期化済みか否か（static） */
    private static boolean initialized = false;
    /** 内蔵サーバ */
    private static HttpServer server;
    /** テスト用ハンドラ */
    private static HttpRequestTestSupportHandler handler;

    /** NTFのDBサポート */
    private final DbAccessTestSupport dbSupport = new DbAccessTestSupport(getClass());

    /** 実行中のテストクラスとメソッド名を保持する */
    @Rule
    public TestDescription testDescription = new TestDescription();

    /**
     * システムリポジトリから設定を取得しHTTPサーバを起動する。
     * テストデータが存在する場合はDBにデータを登録する。
     * 以下2種類のテストデータが対象となる。
     * <ol>
     *   <li>テストクラス単位で共通のデータシート:setUpDb</li>
     *   <li>テストメソッド単位で固有のデータシート：実行中のメソッド名</li>
     * </ol>
     */
    @Before
    public void setUp() {
        // HTTPテスト実行用設定情報の取得
        RestTestConfiguration config = SystemRepository.get(REST_TEST_CONFIGURATION_KEY);
        initializeIfNotYet(config);
        setUpDbIfSheetExists(SETUP_TABLE_SHEET);
        setUpDbIfSheetExists(testDescription.getMethodName());
    }

    /**
     * システムリポジトリから{@link RestMockHttpRequestBuilder}を取得する。
     *
     * @return 取得した{@link RestMockHttpRequestBuilder}
     */
    public RestMockHttpRequestBuilder getHttpRequestBuilder() {
        RestMockHttpRequestBuilder requestBuilder = SystemRepository.get(HTTP_REQUEST_BUILDER_KEY);
        if (requestBuilder == null) {
            throw new IllegalConfigurationException(createNoComponentMessage(HTTP_REQUEST_BUILDER_KEY));
        }
        return requestBuilder;
    }

    /**
     * テストリクエストを内蔵サーバに渡しレスポンスを返す。
     *
     * @param request テストリクエスト
     * @return 内蔵サーバのレスポンス
     */
    public HttpResponse sendRequest(HttpRequest request) {
        return sendRequestWithContext(request, new ExecutionContext());
    }

    /**
     * {@link ExecutionContext}を設定しテストリクエストを内蔵サーバに渡しレスポンスを返す。
     * {@link ExecutionContext}の設定は{@link HttpRequestTestSupportHandler}を利用する。
     *
     * @param request テストリクエスト
     * @param context 実行コンテキスト
     * @return 内蔵サーバのレスポンス
     * @see HttpRequestTestSupportHandler
     */
    public HttpResponse sendRequestWithContext(HttpRequest request, ExecutionContext context) {
        handler.setContext(context);
        return server.handle(request, context);
    }

    /**
     * 初回の場合、内臓サーバを起動する。
     *
     * @param config 設定定義
     */
    private static void initializeIfNotYet(RestTestConfiguration config) {
        if (!initialized) {
            createHttpServer(config);
            initialized = true;
        }
    }

    /**
     * キャッシュした HttpServer をリセットする。
     */
    public static void resetHttpServer() {
        if (initialized) {
            server = null;
            initialized = false;
        }
    }

    /**
     * DBセットアップを実行する。
     *
     * @param sheetName セットアップ対象データの記載されたシート名
     */
    protected void setUpDbIfSheetExists(String sheetName) {
        if (isExisting(sheetName)) {
            setUpDb(sheetName);
        } else {
            LOGGER.logDebug("skipped setUpDb because test data [resource: "
                    + getResourceName(sheetName) + "] was not found.");
        }
    }

    /**
     * HttpServerを生成する。
     *
     * @param config 設定定義
     */
    private static void createHttpServer(RestTestConfiguration config) {
        // HTTPサーバ生成
        server = createHttpServer();
        // HttpTestConfigurationの値を設定する
        server.setTempDirectory(config.getTempDirectory());
        server.setWarBasePaths(getWarBasePaths(config));
        // サーバ起動
        server.startLocal();
        handler = new HttpRequestTestSupportHandler(config);

        // ハンドラキューの準備
        WebFrontController controller = SystemRepository.get(config.getWebFrontControllerKey());
        List<Handler> handlerQueue = new ArrayList<Handler>(controller.getHandlerQueue());
        handler.register(handlerQueue);
        server.setHandlerQueue(handlerQueue);
    }

    /**
     * Warベースパスを取得する。
     *
     * @param config 設定定義
     * @return Warベースパス
     */
    private static List<ResourceLocator> getWarBasePaths(RestTestConfiguration config) {
        String[] baseDirs = config.getWebBaseDir().split(",");
        List<ResourceLocator> basePaths = new ArrayList<ResourceLocator>(baseDirs.length);
        for (String dir : baseDirs) {
            basePaths.add(ResourceLocator.valueOf(
                    "file://" + NablarchTestUtils.toCanonicalPath(dir)));
        }
        return basePaths;
    }

    /**
     * HttpServerのインスタンスを生成する。
     *
     * @return HttpServerのインスタンス
     */
    private static HttpServer createHttpServer() {
        HttpServerFactory factory = SystemRepository.get(HTTP_SERVER_FACTORY_KEY);
        if (factory == null) {
            throw new IllegalConfigurationException(createNoComponentMessage(HTTP_SERVER_FACTORY_KEY));
        }
        return factory.create();
    }

    /**
     * ステータスコードが想定通りであることを表明する。
     *
     * @param message  アサート失敗時のメッセージ
     * @param expected 期待するステータス
     * @param response HTTPレスポンス
     */
    public void assertStatusCode(String message, HttpResponse.Status expected, HttpResponse response) {
        assertStatusCode(message, expected.getStatusCode(), response);
    }

    /**
     * ステータスコードが想定通りであることを表明する。
     *
     * @param message  アサート失敗時のメッセージ
     * @param expected 期待するステータスコード値
     * @param response HTTPレスポンス
     */
    public void assertStatusCode(String message, int expected, HttpResponse response) {
        assertEquals(message + " [HTTP STATUS]", expected, response.getStatusCode());
    }

    /**
     * テストクラスと同じパッケージにあるファイルを読み込み文字列を返す。
     *
     * @param fileName 読み込むファイル名
     * @return ファイル内容の文字列
     */
    protected String readTextResource(String fileName) {
        try {
            URL url = getUrl(testDescription.getTestClassSimpleName() + "/" + fileName);
            File file = new File(url.toURI());
            return read(file);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("couldn't read resource [" + fileName + "]. "
                    + "cause [" + e.getMessage() + "].", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("couldn't read resource [" + fileName + "]. "
                    + "cause [" + e.getMessage() + "].", e);
        }
    }

    /**
     * ファイルを読み込みStringを返す。
     *
     * @param file 読み込むファイル
     * @return ファイル内容の文字列
     * @throws IOException 読み込み失敗時の例外
     */
    protected String read(File file) throws IOException {
        InputStream is = null;
        ByteArrayOutputStream out = null;

        try {
            is = new FileInputStream(file);
            out = new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }

            return out.toString("UTF-8");
        } finally {
            FileUtil.closeQuietly(is, out);
        }
    }

    /**
     * ファイルのURLを取得する。
     *
     * @param fileName 対象のファイル名
     * @return ファイルのURL
     */
    private URL getUrl(String fileName) {
        URL url = testDescription.getTestClass().getResource(fileName);
        if (url == null) {
            throw new IllegalArgumentException("couldn't find resource [" + fileName + "].");
        }
        return url;
    }

    // 委譲メソッド

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#setUpDb(String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @see nablarch.test.core.db.DbAccessTestSupport#setUpDb(String)
     */
    public void setUpDb(String sheetName) {
        dbSupport.setUpDb(sheetName);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#setUpDb(String, String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @param groupId   グループID
     * @see nablarch.test.core.db.DbAccessTestSupport#setUpDb(String, String)
     */
    public void setUpDb(String sheetName, String groupId) {
        dbSupport.setUpDb(sheetName, groupId);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#getListMap(String, String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @param id        ID
     * @return List-Map形式のデータ
     * @see nablarch.test.core.db.DbAccessTestSupport#getListMap(String, String)
     */
    public List<Map<String, String>> getListMap(String sheetName, String id) {
        return dbSupport.getListMap(sheetName, id);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#getListParamMap(String, String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @param id        ID
     * @return List-Map<String, String[]>形式のデータ
     * @see nablarch.test.core.db.DbAccessTestSupport#getListParamMap(String, String)
     */
    public List<Map<String, String[]>> getListParamMap(String sheetName, String id) {
        return dbSupport.getListParamMap(sheetName, id);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#getParamMap(String, String)}への委譲メソッド。
     *
     * @param sheetName シート名
     * @param id        ID
     * @return Map<String, String [ ]>形式のデータ
     * @see nablarch.test.core.db.DbAccessTestSupport#getParamMap(String, String)
     */
    public Map<String, String[]> getParamMap(String sheetName, String id) {
        return dbSupport.getParamMap(sheetName, id);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String)}への委譲メソッド。
     *
     * @param sheetName 期待値を格納したシート名
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String)
     */
    public void assertTableEquals(String sheetName) {
        dbSupport.assertTableEquals(sheetName);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String)}への委譲メソッド。
     *
     * @param sheetName 期待値を格納したシート名
     * @param groupId   グループID（オプション）
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String)
     */
    public void assertTableEquals(String sheetName, String groupId) {
        dbSupport.assertTableEquals(sheetName, groupId);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String)} への委譲メソッド。
     *
     * @param message   比較失敗時のメッセージ
     * @param groupId   グループID（オプション）
     * @param sheetName 期待値を格納したシート名
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String)
     */
    public void assertTableEquals(String message, String sheetName, String groupId) {
        dbSupport.assertTableEquals(message, sheetName, groupId);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String, boolean)} への委譲メソッド。
     *
     * @param message           比較失敗時のメッセージ
     * @param sheetName         期待値を格納したシート名
     * @param groupId           グループID（オプション）
     * @param failIfNoDataFound データが存在しない場合に例外とするかどうか
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String, boolean)
     */
    public void assertTableEquals(String message,
                                  String sheetName,
                                  String groupId,
                                  boolean failIfNoDataFound) {
        dbSupport.assertTableEquals(message, sheetName, groupId, failIfNoDataFound);
    }

    /** テストデータのExcelファイルが存在するか否か */
    private boolean testDataExists = true;

    /**
     * sheetName に合致するリソースが存在するかを判定する。
     *
     * @param sheetName シート名
     * @return 存在する場合true
     */
    private boolean isExisting(String sheetName) {
        if (!testDataExists) {
            return false;
        }
        String resourceName = getResourceName(sheetName);
        String path = getPathOf(resourceName);
        if (path == null) {
            testDataExists = false;
            return false;
        }
        return getSheet(path, sheetName) != null;
    }

    /**
     * テストデータのパスを取得する。
     * 最初にリソースが見つかったテストデータのパスを返す。
     *
     * @param resourceName リソース名
     * @return リソースが存在するパス（存在しない場合、null）
     */
    private String getPathOf(String resourceName) {
        List<String> baseDirs = getTestDataPaths();
        for (String basePath : baseDirs) {
            if (getTestDataParser().isResourceExisting(basePath, resourceName)) {
                return basePath;
            }
        }
        return null;
    }

    /**
     * 引数で渡されたパス配下にある実行中のテストクラスと同名のExcelファイルを読み込み
     * シート名が一致するシートを返す。
     *
     * @param basePath  パス
     * @param sheetName シート名
     * @return 読み込んだ{@link Sheet}
     */
    private Sheet getSheet(String basePath, String sheetName) {
        String filePath = basePath + '/' + testDescription.getTestClassSimpleName();
        File file = new File(filePath + ".xlsx");
        if (!file.exists()) {
            file = new File(filePath + ".xls");
        }
        String absoluteFilePath = file.getAbsolutePath();
        Workbook book;
        InputStream in = null;
        try {
            String uri = new File(absoluteFilePath).toURI().toString();
            in = FileUtil.getResource(uri);
            book = WorkbookFactory.create(in);
        } catch (Exception e) {
            throw new RuntimeException("test data file open failed.", e);
        } finally {
            FileUtil.closeQuietly(in);
        }
        return book.getSheet(sheetName);
    }

    /**
     * リソース名を取得する。<br/>
     *
     * @param sheetName シート名
     * @return リソース名
     */
    private String getResourceName(String sheetName) {
        return testDescription.getTestClassSimpleName() + "/" + sheetName;
    }

    /**
     * テストデータのパスのリストを取得する。
     * リソースルートディレクトリに、クラスのパッケージ階層を付与したものをパスとして返却する。
     * これらのパスがリソースを探索する際の候補となる。
     * <p>
     * 例えば、リソースルートの設定が["test/online;test/batch"]で、テストクラスがfoo.bar.Buzのとき、
     * ["test/online/foo/bar", "test/batch/foo/bar"]が返却される。
     * </p>
     *
     * @return テストデータのパスのリスト
     */
    private List<String> getTestDataPaths() {
        String[] baseDirs = getResourceRootSetting().split(PATH_SEPARATOR);
        String relativePath = packageToPath(testDescription.getTestClass());
        List<String> testDataPaths = new ArrayList<String>(baseDirs.length);
        for (String baseDir : baseDirs) {
            testDataPaths.add(baseDir + '/' + relativePath);
        }
        return testDataPaths;
    }

    /**
     * リソースルートの設定をリポジトリより取得する。<br/>
     * ルートディレクトリが複数設定されている場合、
     * {@link #PATH_SEPARATOR}で区切られている。
     * 明示的に設定がされていない場合はデフォルト設定（{@link #DEFAULT_RESOURCE_ROOT}）を返却する。
     *
     * @return リソースルート設定
     */
    private String getResourceRootSetting() {
        String resourceRootSetting = SystemRepository.get(RESOURCE_ROOT_KEY);
        return (resourceRootSetting == null)
                ? DEFAULT_RESOURCE_ROOT
                : resourceRootSetting;
    }

    /**
     * 与えられたクラスのパッケージからパスに変換する。
     *
     * @param clazz クラス
     * @return パッケージから変換されたパス
     */
    private String packageToPath(Class<?> clazz) {
        String pkg = clazz.getPackage().getName();
        return pkg.replace('.', '/');
    }

    /**
     * テストデータパーサを取得する。
     *
     * @return テストデータパーサ
     */
    public final TestDataParser getTestDataParser() {
        TestDataParser parser = SystemRepository.get(TEST_DATA_PARSER_KEY);
        if (parser == null) {
            throw new IllegalConfigurationException(createNoComponentMessage(TEST_DATA_PARSER_KEY));
        }
        return parser;
    }

    private static String createNoComponentMessage(String componentKey) {
        return "could not find component. name=[" + componentKey + "].";
    }

    /**
     * GETのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest get(String uri) {
        return getHttpRequestBuilder().get(uri);
    }

    /**
     * POSTのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest post(String uri) {
        return getHttpRequestBuilder().post(uri);
    }

    /**
     * PUTのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest put(String uri) {
        return getHttpRequestBuilder().put(uri);
    }

    /**
     * DELETEのHTTPメソッドで{@link RestMockHttpRequest}を生成する。
     *
     * @param uri リクエストURI
     * @return 生成された{@link RestMockHttpRequest}
     */
    public RestMockHttpRequest delete(String uri) {
        return getHttpRequestBuilder().delete(uri);
    }
}
