package nablarch.test.core.http;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.StringUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.HttpServer;
import nablarch.fw.web.HttpServerFactory;
import nablarch.fw.web.MockHttpRequest;
import nablarch.fw.web.ResourceLocator;
import nablarch.fw.web.servlet.WebFrontController;
import nablarch.test.NablarchTestUtils;
import nablarch.test.core.db.DbAccessTestSupport;
import nablarch.test.core.rule.TestDescription;
import nablarch.test.event.TestEventDispatcher;
import org.json.JSONException;
import org.junit.Rule;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RestTestSupport extends TestEventDispatcher {
    /** HTTPサーバファクトリ */
    private static final String HTTP_SERVER_FACTORY_KEY = "httpServerFactory";
    /** RestTestConfigurationのリポジトリキー */
    private static final String REST_TEST_CONFIGURATION = "restTestConfiguration";
    /** テストクラス共通データを定義しているシート名 */
    private static final String SETUP_TABLE_SHEET = "setUpDb";
    /** テストケースを定義しているListMapのID */
    private static final String TEST_SHOTS = "testShots";

    /** LIST_MAPキャッシュ */
    private final Map<String, List<Map<String, String>>> listMapCache = new HashMap<>();

    /** NTFのDBサポート */
    private final DbAccessTestSupport dbSupport = new DbAccessTestSupport(getClass());

    /** 実行中のテストクラスとメソッド名を保持する */
    @Rule
    public TestDescription testDescription = new TestDescription();

    /** 初期化済みか否か */
    private static boolean initialized = false;
    /** 内蔵サーバ */
    private static HttpServer server;
    /** テスト用ハンドラ */
    private static HttpRequestTestSupportHandler handler;

    public void execute() throws IOException, URISyntaxException {
        // HTTPテスト実行用設定情報の取得
        RestTestConfiguration config = (RestTestConfiguration) SystemRepository.getObject(REST_TEST_CONFIGURATION);
        initializeIfNotYet(config);
        setUpDbIfNeed(testDescription.getMethodName(), config);

        List<Map<String, String>> testShots = getCachedListMap(testDescription.getMethodName(), TEST_SHOTS);
        for (Map<String, String> testShot : testShots) {
            RestTestCaseInfo testCaseInfo = new RestTestCaseInfo(testDescription.getMethodName(), testShot);
            executeTestCase(testCaseInfo);
        }
    }

    private void executeTestCase(RestTestCaseInfo testCaseInfo) throws IOException, URISyntaxException {
        // TODO:HttpRequestを組み立てるAPIを公開
        // TODO:Content-Typeとボディの内容にヨテSerializerを
        // TODO:header、method、bodyを設定できる
        // TODO:headerのうちよくあるものはAPI提供。Content-type、Accept
        // TODO:bodyの設定方法はString以外も欲しくなる
        MockHttpRequest httpRequest = createRequest(testCaseInfo);

        ExecutionContext context = new ExecutionContext();
        handler.setContext(context);

        HttpResponse httpResponse = server.handle(httpRequest, context);

        assertStatusCode(httpResponse, testCaseInfo);

        assertJson(httpResponse, testCaseInfo);

        assertTables(testCaseInfo);
    }

    private MockHttpRequest createRequest(RestTestCaseInfo testCaseInfo) {
        String httpMethod = testCaseInfo.getHttpMethod();
        MockHttpRequest httpRequest = new MockHttpRequest();
        if ("POST".equals(httpMethod)) {
            String body = testCaseInfo.getBody();
            String placeHolder = getPlaceHolder(body);
            if (StringUtil.hasValue(placeHolder)) {
                body = buildJSON(testCaseInfo.getSheetName(), placeHolder);
            }
            httpRequest.setBodyReader(new StringReader(body));
            httpRequest.getHeaderMap().put("Content-type", testCaseInfo.getContentType());
        }
        httpRequest.setRequestUri(testCaseInfo.getRequestId());
        httpRequest.setMethod(httpMethod);

        System.out.println(httpRequest.toString());
        return httpRequest;
    }

    private String buildJSON(String sheetName, String id) {
        List<Map<String, String>> paramMapList = getCachedListMap(sheetName, id);
        StringBuilder sb = new StringBuilder();
        if (paramMapList.size() > 1) {
            sb.append("[");
        }
        for (Map<String, String> paramMap : paramMapList) {
            sb.append("{");
            paramMap.forEach((key, value) -> {
                if (sb.length() > 1) {
                    sb.append(",");
                }
                sb.append(quote(key));
                sb.append(":");
                sb.append(getJsonValue(sheetName, value));
            });
            sb.append("}");
        }
        return sb.toString();
    }

    private String quote(String value) {
        return "\"" + value + "\"";
    }

    private String getJsonValue(String sheetName, String value) {
        String placeHolder = getPlaceHolder(value);
        if (StringUtil.isNullOrEmpty(placeHolder)) {
            return quote(value);
        }
        return buildJSON(sheetName, placeHolder);
    }

    private Pattern PLACE_HOLDER = Pattern.compile("\\$\\{(.*?)}");

    private String getPlaceHolder(String target) {
        Matcher m = PLACE_HOLDER.matcher(target);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 初回の場合、内臓サーバを起動する。
     * 初回かつ{@link RestTestConfiguration#shouldSetUpDb()}がtrueの場合共通テストデータをDB登録する。
     */
    private void initializeIfNotYet(RestTestConfiguration config) {
        if (!initialized) {
            createHttpServer(config);
            setUpDbIfNeed(SETUP_TABLE_SHEET, config);
            initialized = true;
        }
    }

    /**
     * DBセットアップを実行する。
     *
     * @param sheetName セットアップ対象データの記載されたシート名
     */
    private void setUpDbIfNeed(String sheetName, RestTestConfiguration config) {
        if (config.shouldSetUpDb()) {
            dbSupport.setUpDb(sheetName);
        }
    }

    /**
     * HttpServerを生成する。
     *
     * @param config HttpTestConfiguration
     */
    private void createHttpServer(RestTestConfiguration config) {
        // HTTPサーバ生成
        server = createHttpServer();
        // HttpTestConfigurationの値を設定する
        server.setTempDirectory(config.getTempDirectory());
        server.setWarBasePaths(getWarBasePaths(config));
        // サーバ起動
        server.startLocal();
        handler = new HttpRequestTestSupportHandler(config);

        // ハンドラキューの準備
        WebFrontController controller = SystemRepository.get("webFrontController");
        List<Handler> handlerQueue = controller.getHandlerQueue();
        handler.register(handlerQueue);
        server.setHandlerQueue(handlerQueue);
    }

    /**
     * Warベースパスを取得する。
     *
     * @param config HttpTestConfiguration
     * @return Warベースパス
     */
    private List<ResourceLocator> getWarBasePaths(RestTestConfiguration config) {
        String[] baseDirs = config.getWebBaseDir().split(",");
        List<ResourceLocator> basePaths = new ArrayList<ResourceLocator>();
        for (String dir : baseDirs) {
            basePaths.add(buildWarDirUri(dir));
        }
        return basePaths;
    }

    /**
     * WarディレクトリのURIを組み立てる。
     *
     * @param pathToWarDir Warディレクトリへの相対パス
     * @return URI
     */
    private ResourceLocator buildWarDirUri(String pathToWarDir) {
        return ResourceLocator.valueOf(
                "file://" + NablarchTestUtils.toCanonicalPath(pathToWarDir));
    }

    /**
     * HttpServerのインスタンスを生成する。
     *
     * @return HttpServerのインスタンス
     */
    protected HttpServer createHttpServer() {
        HttpServerFactory factory = SystemRepository.get(HTTP_SERVER_FACTORY_KEY);
        if (factory == null) {
            throw new IllegalConfigurationException("could not find component. name=[" + HTTP_SERVER_FACTORY_KEY + "].");
        }
        return factory.create();
    }

    /**
     * キャッシュからLIST_MAPを取得する。<br/>
     * キャッシュにない場合は、データシートから取得しメモリ上にキャッシュする。
     *
     * @param sheetName   データシート名
     * @param listMapName LIST_MAP名
     * @return LIST_MAP
     */
    protected List<Map<String, String>> getCachedListMap(String sheetName, String listMapName) {
        if (StringUtil.isNullOrEmpty(listMapName)) {
            throw new IllegalArgumentException("LIST_MAP name parameter is null or empty.");
        }
        String key = sheetName + listMapName;
        if (!listMapCache.containsKey(key)) {
            listMapCache.put(key, getListMap(sheetName, listMapName));
        }
        return listMapCache.get(key);
    }

    private void assertStatusCode(HttpResponse httpResponse, RestTestCaseInfo testCaseInfo) {
        String testCaseName = testCaseInfo.getTestCaseName();
        int expectedStatus = testCaseInfo.getExpectedStatusCode();
        assertStatusCode(testCaseName + "[HTTP STATUS]", expectedStatus, httpResponse);
    }

    /**
     * ステータスコードが想定通りであることを表明する。<br/>
     * 内蔵サーバから戻り値で返却されたHTTPレスポンスがリダイレクトである場合、
     * ステータスコードが303または302であることを表明する。
     * このとき、内蔵サーバから返却されるHTTPレスポンスと比較しないのは、後方互換性を保つためである。
     * （内蔵サーバは、リダイレクト時のステータスコードに'302 FOUND'を使用する）
     * 上記以外の場合は、{@link HttpRequestTestSupportHandler#getStatusCode()}
     * のステータスコードを比較対象とする。
     *
     * @param message  アサート失敗時のメッセージ
     * @param expected 期待するステータスコード値
     * @param response HTTPレスポンス
     * @see HttpRequestTestSupportHandler
     */
    protected void assertStatusCode(String message, int expected, HttpResponse response) {
        // AFW の HttpResponseHandler でレスポンスコードの詰め替えを行っており、300 系のエラー以外の
        // 場合は HttpResponse のステータスコードは 200 になってしまう。
        // このため、 300 系以外のエラーコードは handler から取得、 3XX 系のコードは HttpResponse
        // から取得してアサートする。 
        int handlerStatusCode = handler.getStatusCode();
        int responseStatusCode = response.getStatusCode();
        int actual = is3XXStatusCode(responseStatusCode) ? responseStatusCode : handlerStatusCode;

        assertEquals(message, expected, actual);
    }

    /**
     * 300系の HTTP ステータスコードかどうか判定する
     *
     * @param statusCode 判定対象のHTTPステータスコード
     * @return 300系の HTTP ステータスコードであれば true
     */
    public boolean is3XXStatusCode(int statusCode) {
        return 300 <= statusCode && statusCode <= 399;
    }

    /**
     * 期待値と{@link HttpResponse}から取得したレスポンスボディが等しいことを検証する。
     *
     * @param httpResponse レスポンス
     * @param testCaseInfo テストケース
     * @throws URISyntaxException URLからURIへの変換に失敗した場合に送出される例外
     * @throws IOException        ファイル読み込みに失敗した場合に送出される例外
     */
    private void assertJson(HttpResponse httpResponse, RestTestCaseInfo testCaseInfo) throws URISyntaxException, IOException {
        String testCaseName = testCaseInfo.getTestCaseName();
        String jsonFileName = testCaseInfo.getExpectedBody();
        if (StringUtil.isNullOrEmpty(jsonFileName)) {
            //期待値がない場合は検証をスキップ
            return;
        }
        String expectedJson = readFile(jsonFileName);
        assertJson(testCaseName + "[HTTP BODY]", expectedJson, httpResponse.getBodyString(), JSONCompareMode.LENIENT);
    }
// TODO:業務アプリのテストでJSONAssertを直接呼び出すようにする

    /**
     * 期待値と取得した結果が等しいことを検証する。
     * 処理は{@link JSONAssert#assertEquals(String, String, String, JSONComparator)}に移譲している。
     *
     * @param message  メッセージ
     * @param expected 期待値（JSON文字列）
     * @param actual   検証対象（JSON文字列）
     * @see JSONAssert#assertEquals(String, String, String, JSONComparator)
     */
    public void assertJson(String message, String expected, String actual, JSONCompareMode mode) {
        try {
            JSONAssert.assertEquals(message, expected, actual, mode);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * テストクラスと同じパッケージにあるファイルを読み込み文字列を返す。
     *
     * @param fileName 読み込むファイル名
     * @return ファイル内容の文字列
     * @throws URISyntaxException URLからURIへの変換に失敗した場合に送出される例外
     * @throws IOException        ファイル読み込みに失敗した場合に送出される例外
     */
    private String readFile(String fileName) throws URISyntaxException, IOException {
        URL url = getUrl(testDescription.getTestClassSimpleName() + "/" + fileName);
        Path path = Paths.get(url.toURI());
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * ファイルのURLを取得する。
     * ファイルが見つからない場合はテスト失敗。
     *
     * @param fileName 対象のファイル名
     * @return ファイルのURL
     */
    private URL getUrl(String fileName) {
        URL url = testDescription.getTestClass().getResource(fileName);
        if (url == null) {
            fail("File(" + fileName + ") not found.");
        }
        return url;
    }

    private void assertTables(RestTestCaseInfo testCaseInfo) {
        String testCaseName = testCaseInfo.getTestCaseName();
        String expectedTableId = testCaseInfo.getExpectedTable();
        assertTableEquals(testCaseName + "[Table TEST : " + expectedTableId + "]"
                , testDescription.getMethodName(), expectedTableId, false);
    }

    // 委譲メソッド

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
     * @throws IllegalArgumentException 期待値のデータが存在せず、failIfNoDataFoundが真の場合
     * @see nablarch.test.core.db.DbAccessTestSupport#assertTableEquals(String, String, String, boolean)
     */
    public void assertTableEquals(String message,
                                  String sheetName,
                                  String groupId,
                                  boolean failIfNoDataFound) throws IllegalArgumentException {
        dbSupport.assertTableEquals(message, sheetName, groupId, failIfNoDataFound);
    }
}
