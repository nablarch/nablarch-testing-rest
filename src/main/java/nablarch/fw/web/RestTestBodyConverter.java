package nablarch.fw.web;

/**
 * {@link RestMockHttpRequest}のbodyを文字列に変換するためのインターフェイス。
 */
public interface RestTestBodyConverter {

    /**
     * 引数で渡されたContent-Typeでbodyを変換できるかどうか。
     *
     * @param body      リクエストボディ
     * @param mediaType Content-Type
     * @return 変換できる場合はtrue
     */
    boolean isConvertible(Object body, RestTestMediaType mediaType);

    /**
     * bodyを文字列に変換する。
     *
     * @param body      リクエストボディ
     * @param mediaType Content-Type
     * @return bodyを変換した文字列
     */
    String convert(Object body, RestTestMediaType mediaType);
}
