package nablarch.test.core.rule;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.rules.ExternalResource;

import static org.junit.Assert.fail;

public class HttpServerRunner extends ExternalResource {
    private Server jettyServer;
    private int port;

    public HttpServerRunner(int port) {
        this.port = port;
    }

    @Override
    public void before() throws Throwable {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setBaseResource(Resource.newResource("src/main/webapp"));
        jettyServer = new Server(port);
        jettyServer.setHandler(webAppContext);
        jettyServer.start();
    }

    @Override
    public void after() {
        try {
            jettyServer.stop();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * JettyへのURL
     * @return URL
     */
    public String getBaseUrl() {
        return "http://localhost:" + port;
    }

}
