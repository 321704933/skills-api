package ai.skills.api.common.idempotency;

import ai.skills.api.common.api.ResponseCode;
import ai.skills.api.common.config.IdempotencyProperties;
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
 * 功能：在请求进入控制器前执行幂等校验，避免重复提交。
 * 作者：Devil
 */
@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String REQUEST_KEY_ATTR = IdempotencyInterceptor.class.getName() + ".KEY";
    private static final String REQUEST_TTL_ATTR = IdempotencyInterceptor.class.getName() + ".TTL";

    private final IdempotencyStore idempotencyStore;
    private final IdempotencyProperties properties;

    public IdempotencyInterceptor(IdempotencyStore idempotencyStore, IdempotencyProperties properties) {
        this.idempotencyStore = idempotencyStore;
        this.properties = properties;
    }

    /**
     * 功能：在进入控制器前校验幂等请求头并尝试占位。
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

        Idempotent idempotent = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), Idempotent.class);
        if (idempotent == null) {
            return true;
        }

        String headerName = StringUtils.hasText(idempotent.headerName()) ? idempotent.headerName() : properties.getHeaderName();
        String idempotencyKey = request.getHeader(headerName);
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new BizException(
                    ResponseCode.IDEMPOTENCY_KEY_REQUIRED,
                    ResponseCode.IDEMPOTENCY_KEY_REQUIRED.defaultMessage(),
                    Map.of("headerName", headerName)
            );
        }

        Duration ttl = idempotent.ttlSeconds() > 0 ? Duration.ofSeconds(idempotent.ttlSeconds()) : properties.getTtl();
        String requestKey = request.getMethod() + ":" + request.getRequestURI() + ":" + idempotencyKey.trim();
        boolean acquired = idempotencyStore.tryStart(requestKey, ttl, Instant.now());
        if (!acquired) {
            throw new BizException(
                    ResponseCode.IDEMPOTENT_CONFLICT,
                    ResponseCode.IDEMPOTENT_CONFLICT.defaultMessage(),
                    Map.of("idempotencyKey", idempotencyKey)
            );
        }

        request.setAttribute(REQUEST_KEY_ATTR, requestKey);
        request.setAttribute(REQUEST_TTL_ATTR, ttl);
        response.setHeader(headerName, idempotencyKey);
        return true;
    }

    /**
     * 功能：在请求完成后根据执行结果更新或释放幂等状态。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器
     * @param ex       执行过程中抛出的异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object requestKey = request.getAttribute(REQUEST_KEY_ATTR);
        Object ttl = request.getAttribute(REQUEST_TTL_ATTR);
        if (!(requestKey instanceof String key) || !(ttl instanceof Duration duration)) {
            return;
        }

        if (ex != null || response.getStatus() >= 400) {
            idempotencyStore.release(key);
            return;
        }

        idempotencyStore.markCompleted(key, duration, Instant.now());
    }
}
