package ai.skills.api.image.model;

/**
 * 创建时间：2026/03/16
 * 功能：输出方式枚举。
 * 作者：Devil
 */
public enum OutputMode {

    /**
     * 直接返回二进制流
     */
    BINARY,

    /**
     * 返回 Base64 编码的 JSON
     */
    BASE64;

    /**
     * 功能：根据字符串获取输出方式枚举。
     *
     * @param value 方式字符串（不区分大小写）
     * @return 对应的枚举值，默认返回 BINARY
     */
    public static OutputMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return BINARY;
        }
        String upper = value.trim().toUpperCase();
        try {
            return valueOf(upper);
        } catch (IllegalArgumentException e) {
            return BINARY;
        }
    }
}
