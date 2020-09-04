package nablarch.test.core.http;

import nablarch.core.exception.IllegalConfigurationException;
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
import nablarch.test.core.rule.TestDescription;
import nablarch.test.event.TestEventDispatcher;
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

import static org.junit.Assert.assertEquals;

/**
 * RESTfulウェブサービス用のテストサポートクラス
 */
@Published
public class SimpleRestTestSupport extends TestEventDispatcher {
    /** RestTestConfigurationのリポジトリキー */
    private static final String REST_TEST_CONFIGURATION_KEY = "restTestConfiguration";
    /** RestMockHttpRequestBuilderのリポジトリキー */
    private static final String HTTP_REQUEST_BUILDER_KEY = "restMockHttpRequestBuilder";
    /** HTTPサーバファクトリのリポジトリキー */
    private static final String HTTP_SERVER_FACTORY_KEY = "httpServerFactory";
    /** デフォルトプロセッサのリポジトリキー */
    private static final String PROCESSOR_FACTORY_KEY = "processorFactory";

    /** 内蔵サーバ */
    private static HttpServer server;
    /** テスト用ハンドラ */
    private static HttpRequestTestSupportHandler handler;

    /** 初期化済みか否か（static） */
    private static boolean initialized = false;

    /** デフォルトのプロセッサ（リクエスト・レスポンスともに何もしない） **/
    private RequestResponseProcessor defaultProcessor;
    /** 実行中のテストクラスとメソッド名を保持する */
    @Rule
    public TestDescription testDescription = new TestDescription();

    /**
     * システムリポジトリから設定を取得しHTTPサーバを起動する。
     */
    @Before
    public void setUp() {
        setDefaultProcessor();
        // HTTPテスト実行用設定情報の取得
        RestTestConfiguration config = SystemRepository.get(REST_TEST_CONFIGURATION_KEY);
        initializeIfNotYet(config);
    }

    /**
     * デフォルト{@link RequestResponseProcessor}を設定する。
     * SystemRepositoryに登録されていない場合は何もしない{@link RequestResponseProcessor}を設定する。
     */
    private void setDefaultProcessor() {
        RequestResponseProcessorFactory processorFactory = SystemRepository.get(PROCESSOR_FACTORY_KEY);
        if (processorFactory != null) {
            this.defaultProcessor = processorFactory.create();
        } else {
            this.defaultProcessor = new RequestResponseProcessor() {
                @Override
                public HttpRequest processRequest(HttpRequest request) {
                    return request;
                }

                @Override
                public HttpResponse processResponse(HttpRequest request, HttpResponse response) {
                    return response;
                }
            };
        }
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

    /**
     * テストリクエストを内蔵サーバに渡しレスポンスを返す。
     *
     * @param request テストリクエスト
     * @return 内蔵サーバのレスポンス
     */
    public HttpResponse sendRequest(HttpRequest request) {
        return sendRequest(request, defaultProcessor);
    }

    /**
     * テストリクエストを内蔵サーバに渡しレスポンスを返す。
     *
     * @param request   テストリクエスト
     * @param processor リクエスト・レスポンスに追加処理を実行するプロセッサー
     * @return 内蔵サーバのレスポンス
     */
    public HttpResponse sendRequest(HttpRequest request, RequestResponseProcessor processor) {
        return sendRequestWithContext(request, new ExecutionContext(), processor);
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
        return sendRequestWithContext(request, context, defaultProcessor);
    }

    /**
     * {@link ExecutionContext}を設定しテストリクエストを内蔵サーバに渡しレスポンスを返す。
     * {@link ExecutionContext}の設定は{@link HttpRequestTestSupportHandler}を利用する。
     *
     * @param request   テストリクエスト
     * @param context   実行コンテキスト
     * @param processor リクエスト・レスポンスに追加処理を実行するプロセッサー
     * @return 内蔵サーバのレスポンス
     * @see HttpRequestTestSupportHandler
     */
    public HttpResponse sendRequestWithContext(HttpRequest request, ExecutionContext context,
                                               RequestResponseProcessor processor) {
        request = processor.processRequest(request);
        handler.setContext(context);
        HttpResponse response = server.handle(request, context);
        return processor.processResponse(request, response);
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
     * コンポーネントが見つからない場合のエラーメッセージを組み立てる。
     *
     * @param componentKey コンポーネントのキー
     * @return エラーメッセージ
     */
    protected static String createNoComponentMessage(String componentKey) {
        return "could not find component. name=[" + componentKey + "].";
    }
}
