package nablarch.fw.web;

import nablarch.core.util.StringUtil;

/**
 * MIMEタイプを表すクラス
 */
public class RestTestMediaType {
    /** MIMEタイプ */
    private final String mediaType;

    /**
     * コンストラクタ。
     * 引数を解析してMIMEタイプを設定する。
     *
     * @param contentType Content-Typeヘッダーの値
     */
    public RestTestMediaType(String contentType) {
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
        return obj instanceof RestTestMediaType
                && mediaType.equals(((RestTestMediaType) obj).mediaType);
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