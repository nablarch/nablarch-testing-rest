package nablarch.test.core.http;

/**
 * RESTfulウェブサービステスト向けの{@link HttpTestConfiguration}拡張クラス
 */
public class RestTestConfiguration extends HttpTestConfiguration {
    /** {@link nablarch.fw.web.servlet.WebFrontController}のリポジトリキー */
    private String webFrontControllerKey = "webFrontController";

    /**
     * webFrontControllerKey を取得する。
     *
     * @return webFrontControllerKey
     */
    public String getWebFrontControllerKey() {
        return webFrontControllerKey;
    }

    /**
     * webFrontControllerKey を設定する
     *
     * @param webFrontControllerKey リポジトリキー
     */
    public void setWebFrontControllerKey(String webFrontControllerKey) {
        this.webFrontControllerKey = webFrontControllerKey;
    }
}
