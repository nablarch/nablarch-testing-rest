package nablarch.test.core.rule;

import nablarch.core.util.StringUtil;
import nablarch.test.core.db.DbAccessTestSupport;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SetUpDb implements TestRule {
    private String sheetName;

    public SetUpDb(String sheetName) {
        this.sheetName = sheetName;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                DbAccessTestSupport dbSupport = new DbAccessTestSupport(description.getTestClass());
                setUpSheetName();
                dbSupport.setUpDb(sheetName);
                base.evaluate();
            }

            private void setUpSheetName() {
                if (StringUtil.isNullOrEmpty(sheetName)) {
                    sheetName = description.getMethodName();
                }
                if (StringUtil.isNullOrEmpty(sheetName)) {
                    sheetName = "setUpDb";
                }
            }
        };
    }
}
