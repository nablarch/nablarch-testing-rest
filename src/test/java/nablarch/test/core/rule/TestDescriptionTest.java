package nablarch.test.core.rule;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestDescriptionTest {
    @Rule
    public TestDescription sut = new TestDescription();

    @Test
    public void getTestClassTest() {
        assertEquals(sut.getTestClass(), this.getClass());
    }

    @Test
    public void getTestClassSimpleNameTest() {
        assertEquals(sut.getTestClassSimpleName(), "TestDescriptionTest");
    }

    @Test
    public void getMethodNameTest() {
        assertEquals(sut.getMethodName(), "getMethodNameTest");
    }
}