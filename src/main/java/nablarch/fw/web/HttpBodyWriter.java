package nablarch.fw.web;

import java.io.Writer;

/**
 * {@link RestMockHttpRequest}のbodyを書き込むためのインターフェイス。
 */
public interface HttpBodyWriter {

    /**
     * 引数で渡されたContent-Typeでbodyを書き込めるかどうか。
     *
     * @param body        リクエストボディ
     * @param contentType Content-Type
     * @return 書き込める場合はtrue
     */
    boolean isWritable(Object body, ContentType contentType);

    /**
     * 引数で渡された{@link Writer}にbodyを書き込む。
     *
     * @param body        リクエストボディ
     * @param contentType Content-Type
     */
    String writeValueAsString(Object body, ContentType contentType);
}
