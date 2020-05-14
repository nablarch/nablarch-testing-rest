package nablarch.test.core.http;

/**
 * RESTfulウェブサービス用テスト設定定義クラス。
 */
public class RestTestConfiguration extends HttpTestConfiguration {
    /** DBセットアップ設定 */
    private boolean shouldSetUpDb = false;

    /**
     * DBセットアップ設定を取得する。
     *
     * @return DBセットアップする場合true
     */
    public boolean shouldSetUpDb() {
        return shouldSetUpDb;
    }

    /**
     * DBセットアップ設定を設定する
     *
     * @param shouldSetUpDb DBセットアップするか否か
     */
    public void setShouldSetUpDb(boolean shouldSetUpDb) {
        this.shouldSetUpDb = shouldSetUpDb;
    }

    @Override
    public void setHtmlCheckerConfig(String htmlCheckerConfig) {
        //RESTのテストではHTMLチェックは行わない
    }
}
