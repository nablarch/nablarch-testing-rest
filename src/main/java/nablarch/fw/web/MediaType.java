package nablarch.fw.web;

import nablarch.core.util.StringUtil;

/**
 * MIMEタイプを表すクラス
 */
public class MediaType {
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
        mediaType = parts[0].trim();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MediaType
                && mediaType.equalsIgnoreCase(((MediaType) obj).mediaType);
    }

    @Override
    public int hashCode() {
        return mediaType.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return mediaType;
    }
}
