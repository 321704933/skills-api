package ai.skills.api.captcha.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证码校验结果
 *
 * @author devil_
 * @since 2025-03-17
 */
@Schema(name = "验证码校验结果", description = "验证码校验接口返回的数据")
public record CaptchaVerifyResult(
    @Schema(description = "校验是否通过", example = "true")
    boolean valid,

    @Schema(description = "提示信息", example = "验证码校验通过")
    String message
) {}
