package ai.skills.api.captcha.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证码生成配置
 *
 * @author devil_
 * @since 2025-03-17
 */
@Schema(name = "验证码生成配置", description = "验证码生成接口的配置参数")
public record CaptchaConfig(
    @Schema(description = "验证码类型：line-线干扰，circle-圆圈干扰，shear-扭曲干扰，gif-GIF动态", example = "line", defaultValue = "line")
    String type,

    @Schema(description = "验证码有效期（秒），最小60秒，最大3600秒", example = "300", defaultValue = "300")
    Integer ttl,

    @Schema(description = "验证码字符数，最小4位，最大8位", example = "4", defaultValue = "4")
    Integer length,

    @Schema(description = "图片宽度（像素）", example = "120", defaultValue = "120")
    Integer width,

    @Schema(description = "图片高度（像素）", example = "40", defaultValue = "40")
    Integer height
) {
    /**
     * 获取验证码类型
     */
    public CaptchaType getCaptchaType() {
        return CaptchaType.fromCode(type);
    }

    /**
     * 获取有效期（秒），默认 300 秒（5 分钟）
     */
    public int getTtlSeconds() {
        if (ttl == null || ttl < 60) {
            return 300;
        }
        return Math.min(ttl, 3600);
    }

    /**
     * 获取验证码长度，默认 4 位
     */
    public int getCodeLength() {
        if (length == null || length < 4) {
            return 4;
        }
        return Math.min(length, 8);
    }

    /**
     * 获取图片宽度，默认 120
     */
    public int getWidth() {
        if (width == null || width < 60) {
            return 120;
        }
        return Math.min(width, 300);
    }

    /**
     * 获取图片高度，默认 40
     */
    public int getHeight() {
        if (height == null || height < 20) {
            return 40;
        }
        return Math.min(height, 150);
    }
}
