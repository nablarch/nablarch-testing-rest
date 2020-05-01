package nablarch.test.core.rest;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.web.HttpServerFactory;
import nablarch.fw.web.servlet.WebFrontController;
import nablarch.test.core.db.DbAccessTestSupport;
import nablarch.test.core.http.HttpTestConfiguration;
import nablarch.test.core.rule.TestDescription;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RestTestSupport {
    /** テストクラス共通データを定義しているシート名 */
    private static final String SETUP_TABLE_SHEET = "setUpDb";
    /** 内臓サーバ */
    private static HttpServer httpServer;
    /** 初期化済みか否か */
    private static boolean initialized = false;
    private static nablarch.fw.web.HttpServer server;

    /** NTFのDBサポート */
    private DbAccessTestSupport dbSupport;

    /** 実行中のテストクラスとメソッド名を保持する */
    @Rule
    public TestDescription testDescription = new TestDescription();

    /**
     * 初期化。
     */
    @Before
    public void setUp() throws Throwable {
        dbSupport = new DbAccessTestSupport(testDescription.getTestClass());
        dbSupport.beginTransactions();
        initializeIfNotYet();
        setUpDbIfNeed(testDescription.getMethodName());
    }

    /**
     * 後処理
     */
    @After
    public void tearDown() {
        dbSupport.endTransactions();
        //httpServer.stop();
    }

    /**
     * 初回の場合、内臓サーバを起動する。
     * 初回かつ{@link RestTestSupport#shouldSetUpDb()}がtrueの場合共通テストデータをDB登録する。
     */
    private void initializeIfNotYet() throws Throwable {
        if (!initialized) {
            httpServer = new HttpServer(getPort());
            httpServer.start();
            setUpDbIfNeed(SETUP_TABLE_SHEET);
            initialized = true;
        }
    }

    private void setUpDbIfNeed(String sheetName) {
        if (shouldSetUpDb()) {
            dbSupport.setUpDb(sheetName);
        }
    }

    /**
     * ポート番号。デフォルトは9099
     * 9099を既に使用している場合など必要であればオーバーライドする。
     * @return ポート番号
     */
    protected int getPort() {
        return 9099;
    }

    /**
     * DBセットアップするか否か。
     * デフォルトは false(DBセットアップしない)
     * NTFのDBセットアップを使用する場合はオーバーライドしてtrueを返す。
     * @return DBセットアップする場合、true。しない場合、false。
     */
    protected boolean shouldSetUpDb() {
        return true;
    }

    /**
     * HttpServerを生成する。
     * @param config HttpTestConfiguration
     * @return HTTPサーバ
     */
    protected nablarch.fw.web.HttpServer createHttpServer(HttpTestConfiguration config) {
        // HTTPサーバ生成
        server = createHttpServer();
        // HttpTestConfigurationの値を設定する
        server.setTempDirectory(config.getTempDirectory());
//        server.setWarBasePaths(getWarBasePaths(config));
        // サーバ起動
        server.startLocal();
//        handler = new HttpRequestTestSupportHandler(config);

        // ハンドラキューの準備
        WebFrontController controller = SystemRepository.get("webFrontController");
        server.setHandlerQueue(controller.getHandlerQueue());
        return server;
    }

    private static final String HTTP_SERVER_FACTORY_KEY = "httpServerFactory";

    /**
     * HttpServerのインスタンスを生成する。
     * @return HttpServerのインスタンス
     */
    protected nablarch.fw.web.HttpServer createHttpServer() {
        HttpServerFactory factory = SystemRepository.get(HTTP_SERVER_FACTORY_KEY);
        if (factory == null) {
            throw new IllegalConfigurationException("could not find component. name=[" + HTTP_SERVER_FACTORY_KEY + "].");
        }
        return factory.create();
    }

    public void execute() {
        List<Map<String, String>> testShots = getListMap(testDescription.getMethodName(), "testShots");
        testShots.forEach(this::executeTestShot);
    }

    private void executeTestShot(Map<String, String> testShot) {
        String url = testShot.get("targetUrl");
        int expectedStatus = Integer.parseInt(testShot.get("expectedStatusCode"));
        String httpMethod = testShot.get("");
    }

    /**
     * 内臓サーバ向けの{@link WebTarget}を作成する。
     * @return URLが設定されたWebTarget
     */
    public WebTarget target(String path) {
        return ClientBuilder.newClient()
                .target(httpServer.getBaseUrl())
                .path(path);
    }
//
//    public WebTarget multiPartTarget(String path) {
//        return ClientBuilder.newBuilder()
//                .register(MultiPartFeature.class)
//                .build()
//                .target(httpServer.getBaseUrl())
//                .path(path);
//    }
//
//    public FormDataMultiPart createMultiPart(String fileName) throws URISyntaxException {
//        URL url = getUrl(fileName);
//        FileDataBodyPart filePart = new FileDataBodyPart("file", new File(url.toURI()));
//        return (FormDataMultiPart) new FormDataMultiPart().bodyPart(filePart);
//    }

    /**
     * レスポンスステータスが{@link Response.Status#OK}と等しいかを検証する。
     * @param response 検証対象のレスポンス
     */
    public void assertStatusOK(Response response) {
        assertStatus(Response.Status.OK, response);
    }

    /**
     * レスポンスステータスが{@link Response.Status#BAD_REQUEST}と等しいかを検証する。
     * @param response 検証対象のレスポンス
     */
    public void assertStatusBadRequest(Response response) {
        assertStatus(Response.Status.BAD_REQUEST, response);
    }

    /**
     * {@link Response}のレスポンスステータスが{@link Response.Status}と等しいかを検証する。
     * @param expected 期待される{@link Response.Status}
     * @param response 検証対象のレスポンス
     */
    public void assertStatus(Response.Status expected, Response response) {
        assertEquals(expected.getStatusCode(), response.getStatus());
    }

    /**
     * {@link Response}をJSONとして解析し実行中のテストメソッド名に該当するJSONファイルと等しいかを検証する。
     * 比較モードは{@link JSONCompareMode#LENIENT}固定
     * @param response 検証対象のレスポンス
     * @see RestTestSupport#assertJson(String, Response, JSONCompareMode)
     * @see RestTestSupport#readJson()
     * @see JSONCompareMode#LENIENT
     */
    public void assertJson(Response response) throws IOException, URISyntaxException {
        assertJson(readJson(), response, JSONCompareMode.LENIENT);
    }

    public void assertJson(String fileName, Response response) throws IOException, URISyntaxException {
        assertJson(readJson(fileName), response, JSONCompareMode.LENIENT);
    }

    /**
     * 期待値と{@link Response}から取得した結果が等しいことを検証する。
     * @param expected 期待値（JSON文字列）
     * @param response 検証対象のレスポンス
     */
    public void assertJson(String expected, Response response, JSONCompareMode mode) {
        assertJson(expected, response.readEntity(String.class), mode);
    }

    /**
     * 期待値と{@link Response}から取得した結果が等しいことを検証する。
     * 処理は{@link JSONAssert#assertEquals(String, String, JSONComparator)}に移譲している。
     * @param expected 期待値（JSON文字列）
     * @param actual   検証対象（JSON文字列）
     * @see JSONAssert#assertEquals(String, String, JSONComparator)
     */
    public void assertJson(String expected, String actual, JSONCompareMode mode) {
        try {
            JSONAssert.assertEquals(expected, actual, mode);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private URL getUrl(String fileName) {
        URL url = testDescription.getTestClass().getResource(fileName);
        if (url == null) {
            fail("File(" + fileName + ") not found.");
        }
        return url;
    }

    /**
     * 実行中のテストメソッド名に該当するJSONファイルを読み込み{@link String}に変換する。
     * 読み込むファイルは実行中のテストクラス・メソッドに依存する。
     * 例）com.example.FooTest#testBarというテストメソッドを実行する場合
     * src/test/resources/com/example/FooTest/testBar.json というファイルが読み込まれる。
     */
    private String readJson() throws URISyntaxException, IOException {
        String fileName = testDescription.getTestClassSimpleName()
                + "/" + testDescription.getMethodName() + ".json";
        return readJson(fileName);
    }

    private String readJson(String fileName) throws URISyntaxException, IOException {
        URL url = getUrl(fileName);
        Path path = Paths.get(url.toURI());
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#getListMap(String, String)}への委譲メソッド。
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
     * @param sheetName 期待値を格納したシート名
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String)
     */
    public void assertTableEquals(String sheetName) {
        dbSupport.assertTableEquals(sheetName);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String)}への委譲メソッド。
     * @param sheetName 期待値を格納したシート名
     * @param groupId   グループID（オプション）
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String)
     */
    public void assertTableEquals(String sheetName, String groupId) {
        dbSupport.assertTableEquals(sheetName, groupId);
    }

    /**
     * {@link nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String)} への委譲メソッド。
     * @param message   比較失敗時のメッセージ
     * @param groupId   グループID（オプション）
     * @param sheetName 期待値を格納したシート名
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String)
     */
    public void assertTableEquals(String message, String sheetName, String groupId) {
        dbSupport.assertTableEquals(message, sheetName, groupId);
    }
}
