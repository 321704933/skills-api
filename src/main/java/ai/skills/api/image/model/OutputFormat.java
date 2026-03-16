package ai.skills.api.image.model;

/**
 * 创建时间：2026/03/16
 * 功能：支持的输出格式枚举。
 * 作者：Devil
 */
public enum OutputFormat {

    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    WEBP("webp", "image/webp");

    private final String extension;
    private final String contentType;

    OutputFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * 功能：根据字符串获取输出格式枚举。
     *
     * @param value 格式字符串（不区分大小写）
     * @return 对应的枚举值，若无效则返回 null
     */
    public static OutputFormat fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String upper = value.trim().toUpperCase();
        for (OutputFormat format : values()) {
            if (format.name().equals(upper)) {
                return format;
            }
        }
        // 支持 JPEG 别名
        if ("JPEG".equals(upper)) {
            return JPG;
        }
        return null;
    }
}
