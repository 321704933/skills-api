package ai.skills.api.captcha.model;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 验证码校验请求
 *
 * @author devil_
 * @since 2025-03-17
 */
@Schema(name = "验证码校验请求", description = "验证码校验接口请求参数")
public record CaptchaVerifyRequest(
    @Schema(description = "验证码唯一标识", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码ID不能为空")
    String captchaId,

    @Schema(description = "用户输入的验证码", example = "A3X9", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    String captcha
) {}
