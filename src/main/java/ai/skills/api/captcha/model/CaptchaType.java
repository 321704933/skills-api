package ai.skills.api.captcha.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证码类型枚举
 *
 * @author devil_
 * @since 2025-03-17
 */
@Schema(name = "验证码类型", description = "验证码类型枚举")
public enum CaptchaType {

    @Schema(description = "线干扰验证码")
    LINE("line", "线干扰验证码"),

    @Schema(description = "圆圈干扰验证码")
    CIRCLE("circle", "圆圈干扰验证码"),

    @Schema(description = "扭曲干扰验证码")
    SHEAR("shear", "扭曲干扰验证码"),

    @Schema(description = "GIF 动态验证码")
    GIF("gif", "GIF 动态验证码");

    private final String code;
    private final String description;

    CaptchaType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static CaptchaType fromCode(String code) {
        if (code == null) {
            return LINE; // 默认线干扰
        }
        for (CaptchaType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return LINE;
    }
}
