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
        assertEquals(this.getClass(), sut.getTestClass());
    }

    /**
     * {@link TestDescription#getTestClassSimpleName()}のテスト
     */
    @Test
    public void getTestClassSimpleNameTest() {
        assertEquals("TestDescriptionTest", sut.getTestClassSimpleName());
    }

    /**
     * {@link TestDescription#getMethodName()}のテスト
     */
    @Test
    public void getMethodNameTest() {
        assertEquals("getMethodNameTest", sut.getMethodName());
    }
}