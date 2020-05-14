package nablarch.fw.web;

import java.io.IOException;
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
    boolean isWritable(Object body, String contentType);

    /**
     * 引数で渡された{@link Writer}にbodyを書き込む。
     *
     * @param body        リクエストボディ
     * @param contentType Content-Type
     * @param out         リクエストボディを書き込む{@link Writer}
     * @throws IOException 書き込みに失敗した場合に送出される例外
     */
    void write(Object body, String contentType, Writer out) throws IOException;
}
