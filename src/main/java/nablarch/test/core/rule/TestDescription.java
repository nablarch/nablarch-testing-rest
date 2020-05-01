package nablarch.test.core.rule;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * テスト対象のクラスとメソッド名を保持するルール
 */
public class TestDescription extends TestWatcher {
    private Class<?> clazz;
    private String methodName;

    public TestDescription() {
    }

    protected void starting(Description d) {
        clazz = d.getTestClass();
        methodName = d.getMethodName();
    }

    public Class<?> getTestClass() {
        return clazz;
    }

    public String getTestClassSimpleName() {
        return clazz.getSimpleName();
    }

    public String getMethodName() {
        return methodName;
    }
}
