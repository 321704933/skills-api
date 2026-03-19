package ai.skills.api.captcha.service;

import ai.skills.api.captcha.model.CaptchaConfig;
import ai.skills.api.captcha.model.CaptchaResult;
import ai.skills.api.captcha.model.CaptchaType;
import ai.skills.api.captcha.model.CaptchaVerifyResult;
import ai.skills.api.common.cache.CacheService;
import cn.hutool.captcha.*;
import cn.hutool.core.codec.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Duration;
import java.util.UUID;

/**
 * 验证码服务
 * <p>
 * 使用 Hutool 生成图形验证码，通过缓存存储验证码进行校验
 * 支持多种验证码类型：线干扰、圆圈干扰、扭曲干扰、GIF 动态验证码
 *
 * @author devil_
 * @since 2025-03-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    /**
     * 缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "captcha:";

    /** 缓存服务 */
    private final CacheService cacheService;

    /**
     * 干扰元素数量
     */
    private static final int INTERFERENCE_COUNT = 5;

    /**
     * 生成验证码（使用默认配置）
     *
     * @return 验证码生成结果，包含唯一标识和 Base64 编码的图片
     */
    public CaptchaResult generate() {
        return generate(new CaptchaConfig(null, null, null, null, null));
    }

    /**
     * 生成验证码（使用自定义配置）
     *
     * @param config 验证码配置
     * @return 验证码生成结果，包含唯一标识和 Base64 编码的图片
     */
    public CaptchaResult generate(CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();
        int codeCount = config.getCodeLength();
        int ttlSeconds = config.getTtlSeconds();
        CaptchaType type = config.getCaptchaType();

        // 根据类型创建对应的验证码
        AbstractCaptcha captcha = createCaptcha(type, width, height, codeCount);

        // 设置字体
        captcha.setFont(new Font("Arial", Font.BOLD, (int) (height * 0.7)));

        // 生成验证码
        captcha.createCode();

        // 获取验证码文本（转为大写，忽略大小写校验）
        String code = captcha.getCode().toUpperCase();

        // 生成唯一标识
        String captchaId = UUID.randomUUID().toString();

        // 将验证码存入缓存
        String cacheKey = CACHE_KEY_PREFIX + captchaId;
        cacheService.set(cacheKey, code, Duration.ofSeconds(ttlSeconds));

        // 获取图片 Base64 编码
        String mimeType = type == CaptchaType.GIF ? "image/gif" : "image/png";
        String imageBase64 = "data:" + mimeType + ";base64," + Base64.encode(captcha.getImageBytes());

        log.debug("生成验证码成功，captchaId: {}, type: {}, code: {}", captchaId, type.getCode(), code);

        return new CaptchaResult(captchaId, imageBase64);
    }

    /**
     * 根据类型创建验证码对象
     *
     * @param type      验证码类型
     * @param width     宽度
     * @param height    高度
     * @param codeCount 验证码位数
     * @return 验证码对象
     */
    private AbstractCaptcha createCaptcha(CaptchaType type, int width, int height, int codeCount) {
        return switch (type) {
            case CIRCLE -> {
                CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(width, height, codeCount, INTERFERENCE_COUNT);
                yield captcha;
            }
            case SHEAR -> {
                ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(width, height, codeCount, INTERFERENCE_COUNT);
                yield captcha;
            }
            case GIF -> {
                GifCaptcha captcha = CaptchaUtil.createGifCaptcha(width, height, codeCount);
                yield captcha;
            }
            default -> {
                LineCaptcha captcha = CaptchaUtil.createLineCaptcha(width, height, codeCount, INTERFERENCE_COUNT);
                yield captcha;
            }
        };
    }

    /**
     * 校验验证码
     *
     * @param captchaId 验证码唯一标识
     * @param inputCode 用户输入的验证码
     * @return 校验结果
     */
    public CaptchaVerifyResult verify(String captchaId, String inputCode) {
        String cacheKey = CACHE_KEY_PREFIX + captchaId;

        // 从缓存获取存储的验证码
        String storedCode = cacheService.get(cacheKey);

        // 验证码不存在或已过期
        if (storedCode == null) {
            log.warn("验证码不存在或已过期，captchaId: {}", captchaId);
            return new CaptchaVerifyResult(false, "验证码不存在或已过期");
        }

        // 校验验证码（忽略大小写）
        boolean valid = storedCode.equalsIgnoreCase(inputCode);

        // 无论校验成功与否，都删除验证码（一次性使用）
        cacheService.delete(cacheKey);

        if (valid) {
            log.debug("验证码校验通过，captchaId: {}", captchaId);
            return new CaptchaVerifyResult(true, "验证码校验通过");
        } else {
            log.warn("验证码校验失败，captchaId: {}, 输入: {}, 正确: {}", captchaId, inputCode, storedCode);
            return new CaptchaVerifyResult(false, "验证码错误");
        }
    }
}
