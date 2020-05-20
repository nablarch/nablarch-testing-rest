package nablarch.test.core.http;

/**
 * RESTfulウェブサービス用テスト設定定義クラス。
 */
public class RestTestConfiguration extends HttpTestConfiguration {
    @Override
    public void setHtmlCheckerConfig(String htmlCheckerConfig) {
        //RESTのテストではHTMLチェックは行わない
    }
}
