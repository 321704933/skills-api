package ai.skills.api.common.config;

import ai.skills.api.common.idempotency.IdempotencyInterceptor;
import ai.skills.api.common.ratelimit.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 创建时间：2026/03/12
 * 功能：注册 Web MVC 拦截器，统一接入幂等和限流逻辑。
 * 作者：Devil
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final IdempotencyInterceptor idempotencyInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(
            IdempotencyInterceptor idempotencyInterceptor,
            RateLimitInterceptor rateLimitInterceptor
    ) {
        this.idempotencyInterceptor = idempotencyInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    /**
     * 功能：向 Spring MVC 注册全局拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotencyInterceptor).order(0);
        registry.addInterceptor(rateLimitInterceptor).order(1);
    }
}
