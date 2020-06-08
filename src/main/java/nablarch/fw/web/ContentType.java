package nablarch.fw.web;

import nablarch.core.util.StringUtil;

/**
 * Content-Typeを表すクラス
 */
public class ContentType {
    /** MIMEタイプ */
    private final String mediaType;
    /** 文字コード */
    private String charset;
    /** バウンダリー */
    private String boundary;

    /**
     * コンストラクタ。
     * 引数を解析してMIMEタイプと文字コード、バウンダリーを設定する。
     * 文字コード、バウンダリーが指定されていない場合はMIMEタイプのみ設定する。
     *
     * @param rawContentType Content-Typeヘッダーの値
     */
    public ContentType(String rawContentType) {
        if (StringUtil.isNullOrEmpty(rawContentType)) {
            throw new IllegalArgumentException("content type must not be empty.");
        }

        String[] parts = rawContentType.split(";");
        if (parts.length > 2) {
            throw new IllegalArgumentException("[" + rawContentType + "] is invalid format.");
        }

        this.mediaType = parts[0].trim();
        if (parts.length == 1) {
            return;
        }

        String second = parts[1].trim();
        if (second.toUpperCase().startsWith("CHARSET")) {
            this.charset = second;
        } else if (second.toUpperCase().startsWith("BOUNDARY")) {
            this.boundary = second;
        }
    }

    /**
     * MIMEタイプが一致するかどうかを判定する。
     * 前後の空白および大文字小文字は区別しない。
     *
     * @param mediaType MIMEタイプ
     * @return 一致する場合true
     */
    public boolean is(String mediaType) {
        return this.mediaType.toUpperCase().equals(mediaType.trim().toUpperCase());
    }

    /**
     * mediaType を取得する。
     *
     * @return mediaType
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * charset を取得する。
     *
     * @return charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * boundary を取得する。
     *
     * @return boundary
     */
    public String getBoundary() {
        return boundary;
    }

    @Override
    public String toString() {
        if (StringUtil.hasValue(charset)) {
            return mediaType + "; " + charset;
        }
        if (StringUtil.hasValue(boundary)) {
            return mediaType + "; " + boundary;
        }
        return mediaType;
    }
}
