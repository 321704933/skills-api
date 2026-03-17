package ai.skills.api.captcha.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证码生成结果
 *
 * @author devil_
 * @since 2025-03-17
 */
@Schema(name = "验证码生成结果", description = "验证码生成接口返回的数据")
public record CaptchaResult(
    @Schema(description = "验证码唯一标识", example = "550e8400-e29b-41d4-a716-446655440000")
    String captchaId,

    @Schema(description = "验证码图片 Base64 编码", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    @JsonProperty("imageBase64")
    String imageBase64
) {}
