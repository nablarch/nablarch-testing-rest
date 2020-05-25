package nablarch.test.core.http.result;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static org.junit.Assert.assertEquals;

/**
 * JSONPathでのアサーションヘルパー
 */
public class JsonPathAssertHelper {
    /** JSON文字列をパースしたコンテキスト */
    private final DocumentContext context;

    /** 引数のJSON文字列を読み込みコンテキストを保持するコンストラクタ */
    public JsonPathAssertHelper(String json) {
        context = JsonPath.parse(json);
    }

    /**
     * JSONPathで取り出した値が期待値と一致することを表明する。
     *
     * @param expected 期待値
     * @param jsonPath JSONPath
     * @param <T>      取り出す値の型
     * @return {@link JsonPathAssertHelper}自身
     */
    public <T> JsonPathAssertHelper assertEqualsByJsonPath(T expected, String jsonPath) {
        return assertEqualsByJsonPath("", expected, jsonPath);
    }

    /**
     * JSONPathで取り出した値が期待値と一致することを表明する。
     *
     * @param message  メッセージ
     * @param expected 期待値
     * @param jsonPath JSONPath
     * @param <T>      取り出す値の型
     * @return {@link JsonPathAssertHelper}自身
     */
    public <T> JsonPathAssertHelper assertEqualsByJsonPath(String message, T expected, String jsonPath) {
        T actual = context.read(jsonPath);
        assertEquals(message + "[JSON path \"" + jsonPath + "\"]", expected, actual);
        return this;
    }
}
