package nablarch.test.core.rest;

import nablarch.core.repository.SystemRepository;
import nablarch.test.core.http.HttpTestConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class HttpServer {
    /** ポート番号 */
    private final int port;
    /** 内蔵Jettyサーバ */
    private Server jettyServer;
    /** HttpTestConfigurationのリポジトリキー */
    private static final String HTTP_TEST_CONFIGURATION = "httpTestConfiguration";

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() throws Throwable {
        // HTTPテスト実行用設定情報の取得
        HttpTestConfiguration config = (HttpTestConfiguration) SystemRepository.getObject(HTTP_TEST_CONFIGURATION);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setBaseResource(Resource.newResource(config.getWebBaseDir()));

        jettyServer = new Server(port);
        jettyServer.setHandler(webAppContext);
        jettyServer.start();
    }

    public void stop() throws Exception {
        jettyServer.stop();
    }

    /**
     * JettyへのURL
     * @return URL
     */
    public String getBaseUrl() {
        return "http://localhost:" + port;
    }
}
