package nablarch.test.core.rule;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@link TestDescription}のテストクラス。
 */
public class TestDescriptionTest {
    @Rule
    public TestDescription sut = new TestDescription();

    /**
     * {@link TestDescription#getTestClass()}のテスト
     */
    @Test
    public void getTestClassTest() {
        assertEquals(sut.getTestClass(), this.getClass());
    }

    /**
     * {@link TestDescription#getTestClassSimpleName()}のテスト
     */
    @Test
    public void getTestClassSimpleNameTest() {
        assertEquals(sut.getTestClassSimpleName(), "TestDescriptionTest");
    }

    /**
     * {@link TestDescription#getMethodName()}のテスト
     */
    @Test
    public void getMethodNameTest() {
        assertEquals(sut.getMethodName(), "getMethodNameTest");
    }
}