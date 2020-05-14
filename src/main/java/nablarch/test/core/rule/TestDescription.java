package nablarch.test.core.rule;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * テスト対象のクラスとメソッド名を保持するルール
 */
public class TestDescription extends TestWatcher {
    /** テストクラス */
    private Class<?> clazz;
    /** テストメソッド名 */
    private String methodName;

    @Override
    protected void starting(Description d) {
        clazz = d.getTestClass();
        methodName = d.getMethodName();
    }

    /**
     * 実行中のテストクラスを取得する。
     *
     * @return 実行中のテストクラス
     */
    public Class<?> getTestClass() {
        return clazz;
    }

    /**
     * 実行中のテストクラス名を取得する。
     *
     * @return 実行中のテストクラス名
     */
    public String getTestClassSimpleName() {
        return clazz.getSimpleName();
    }

    /**
     * 実行中のテストメソッド名を取得する。
     *
     * @return 実行中のテストメソッド名
     */
    public String getMethodName() {
        return methodName;
    }
}
