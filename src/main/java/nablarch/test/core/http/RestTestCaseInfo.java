package nablarch.test.core.http;

import java.util.Map;

public class RestTestCaseInfo {
    private static final String REQUEST_ID = "requestId";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String DESCRIPTION = "description";
    private static final String EXPECTED_STATUS_CODE = "expectedStatusCode";
    private static final String EXPECTED_BODY = "expectedBody";
    private static final String EXPECTED_TABLE = "expectedTable";
    private static final String CONTENT_TYPE = "contentType";
    private static final String BODY = "body";

    private final String sheetName;
    private final Map<String, String> testCaseParams;

    public RestTestCaseInfo(String sheetName, Map<String, String> testCaseParams) {
        this.sheetName = sheetName;
        this.testCaseParams = testCaseParams;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getRequestId() {
        return getValue(testCaseParams, REQUEST_ID);
    }

    public String getHttpMethod() {
        return getValue(testCaseParams, HTTP_METHOD);
    }

    public String getTestCaseName() {
        return getValue(testCaseParams, DESCRIPTION);
    }

    public int getExpectedStatusCode() {
        return Integer.parseInt(getValue(testCaseParams, EXPECTED_STATUS_CODE));
    }

    public String getExpectedBody() {
        return getValue(testCaseParams, EXPECTED_BODY);
    }

    public String getExpectedTable() {
        return getValue(testCaseParams, EXPECTED_TABLE);
    }

    public String getContentType() {
        return getValue(testCaseParams, CONTENT_TYPE);
    }

    public String getBody() {
        return getValue(testCaseParams, BODY);
    }

    /**
     * LIST_MAPから取得したレコードから、指定したカラム名に対応する値を取得する<br/>
     *
     * @param row        行レコード(LIST_MAPの各要素）
     * @param columnName カラム名
     * @return 指定したカラム名に対応する値
     */
    private String getValue(Map<String, String> row, String columnName) {
        if (!row.containsKey(columnName)) {
            throw new IllegalArgumentException("column '" + columnName + "' is not defined.");
        }
        return row.get(columnName);
    }

}
