package ai.skills.api.captcha.controller;

import ai.skills.api.captcha.model.CaptchaConfig;
import ai.skills.api.captcha.model.CaptchaResult;
import ai.skills.api.captcha.model.CaptchaVerifyRequest;
import ai.skills.api.captcha.model.CaptchaVerifyResult;
import ai.skills.api.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码控制器
 * <p>
 * 提供验证码生成和校验接口，支持多种验证码类型和自定义配置
 *
 * @author devil_
 * @since 2025-03-17
 */
@Tag(name = "验证码")
@RestController
@RequestMapping("/api/v1/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 生成验证码（使用默认配置）
     * <p>
     * 默认配置：线干扰验证码，4 位字符，有效期 5 分钟，图片尺寸 120x40
     */
    @Operation(summary = "生成验证码（默认配置）", description = "使用默认配置生成图形验证码：线干扰、4位字符、有效期5分钟")
    @GetMapping("/generate")
    public CaptchaResult generate() {
        return captchaService.generate();
    }

    /**
     * 生成验证码（使用自定义配置）
     * <p>
     * 支持配置验证码类型、有效期、字符数、图片尺寸
     */
    @Operation(summary = "生成验证码（自定义配置）", description = "使用自定义配置生成验证码，支持设置类型、有效期、字符数、图片尺寸")
    @PostMapping("/generate")
    public CaptchaResult generateWithConfig(@RequestBody CaptchaConfig config) {
        return captchaService.generate(config);
    }

    /**
     * 校验验证码
     * <p>
     * 根据验证码ID和用户输入的验证码进行校验
     */
    @Operation(summary = "校验验证码", description = "校验用户输入的验证码是否正确")
    @PostMapping("/verify")
    public CaptchaVerifyResult verify(@Valid @RequestBody CaptchaVerifyRequest request) {
        return captchaService.verify(request.captchaId(), request.captcha());
    }
}
