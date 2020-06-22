package nablarch.fw.web;

import nablarch.core.util.StringUtil;

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
    boolean isConvertible(Object body, MediaType mediaType);

    /**
     * bodyを文字列に変換する。
     *
     * @param body      リクエストボディ
     * @param mediaType Content-Type
     * @return bodyを変換した文字列
     */
    String convert(Object body, MediaType mediaType);

    /**
     * MIMEタイプを表すクラス
     */
    class MediaType {
        /** MIMEタイプ */
        private final String mediaType;

        /**
         * コンストラクタ。
         * 引数を解析してMIMEタイプを設定する。
         *
         * @param contentType Content-Typeヘッダーの値
         */
        public MediaType(String contentType) {
            if (StringUtil.isNullOrEmpty(contentType)) {
                throw new IllegalArgumentException("content type must not be empty.");
            }

            String[] parts = contentType.split(";");
            String mediaType = parts[0].trim().toLowerCase();

            if (StringUtil.isNullOrEmpty(mediaType)) {
                throw new IllegalArgumentException("media type must not be empty.");
            }

            this.mediaType = mediaType;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof MediaType
                    && mediaType.equals(((MediaType) obj).mediaType);
        }

        @Override
        public int hashCode() {
            return mediaType.hashCode();
        }

        @Override
        public String toString() {
            return mediaType;
        }
    }
}
