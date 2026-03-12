package ai.skills.api.common.ratelimit;

import ai.skills.api.common.api.ResponseCode;
import ai.skills.api.common.config.RateLimitProperties;
import ai.skills.api.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * 创建时间：2026/03/12
 * 功能：在请求进入控制器前执行限流校验，并写入限流响应头。
 * 作者：Devil
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final RateLimitProperties properties;

    public RateLimitInterceptor(RateLimiter rateLimiter, RateLimitProperties properties) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    /**
     * 功能：在控制器执行前完成限流判定。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器
     * @return 是否继续执行后续流程
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.isEnabled() || !(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimited rateLimited = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RateLimited.class);
        if (rateLimited == null) {
            return true;
        }

        RateLimitRule rule = resolveRule(rateLimited);
        String clientIdentifier = resolveClientIdentifier(request);
        String key = request.getMethod() + ":" + request.getRequestURI() + ":" + clientIdentifier;
        RateLimitDecision decision = rateLimiter.acquire(key, rule, Instant.now());

        response.setHeader("X-RateLimit-Limit", String.valueOf(rule.permits()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        response.setHeader("X-RateLimit-Reset-At", decision.resetAt().toString());

        if (!decision.allowed()) {
            throw new BizException(
                    ResponseCode.RATE_LIMITED,
                    ResponseCode.RATE_LIMITED.defaultMessage(),
                    Map.of("resetAt", decision.resetAt().toString(), "clientIdentifier", clientIdentifier)
            );
        }

        return true;
    }

    /**
     * 功能：把注解配置转换为可执行的限流规则。
     *
     * @param rateLimited 限流注解
     * @return 限流规则
     */
    private RateLimitRule resolveRule(RateLimited rateLimited) {
        int permits = rateLimited.permits() > 0 ? rateLimited.permits() : properties.getDefaultPermits();
        Duration window = rateLimited.windowSeconds() > 0
                ? Duration.ofSeconds(rateLimited.windowSeconds())
                : properties.getDefaultWindow();
        return new RateLimitRule(permits, window);
    }

    /**
     * 功能：解析客户端标识，优先取代理层透传请求头，降级使用远端地址。
     *
     * @param request 当前请求
     * @return 客户端标识
     */
    private String resolveClientIdentifier(HttpServletRequest request) {
        String fromHeader = request.getHeader(properties.getClientIdentifierHeader());
        if (StringUtils.hasText(fromHeader)) {
            return fromHeader.split(",")[0].trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
    }
}
