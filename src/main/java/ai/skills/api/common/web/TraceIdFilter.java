package ai.skills.api.common.web;

import ai.skills.api.common.config.WebProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 创建时间：2026/03/12
 * 功能：为每次请求生成或透传 traceId（链路追踪编号），并写入响应头与日志上下文。
 * 作者：Devil
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private final WebProperties webProperties;

    public TraceIdFilter(WebProperties webProperties) {
        this.webProperties = webProperties;
    }

    /**
     * 功能：在过滤器链最前面处理 traceId（链路追踪编号）。
     * 说明：MDC（Mapped Diagnostic Context，映射诊断上下文）用于把 traceId 注入日志上下文。
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceHeaderName = webProperties.getTraceHeaderName();
        String traceId = request.getHeader(traceHeaderName);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        TraceContext.setTraceId(traceId);
        MDC.put("traceId", traceId);
        response.setHeader(traceHeaderName, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
            TraceContext.clear();
        }
    }
}
