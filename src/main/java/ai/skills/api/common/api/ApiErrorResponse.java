package ai.skills.api.common.api;

import ai.skills.api.common.web.TraceContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 创建时间：2026/03/12
 * 功能：定义 API（应用程序编程接口）失败响应的统一结构。
 * 作者：Devil
 *
 * @param success   是否成功
 * @param code      错误码
 * @param message   错误说明
 * @param status    响应状态枚举
 * @param timestamp 响应时间
 * @param traceId   traceId（链路追踪编号）
 * @param path      请求路径
 * @param errors    字段级校验错误列表
 * @param details   扩展错误明细
 */
public record ApiErrorResponse(
        boolean success,
        Integer code,
        String message,
        ResponseStatus status,
        Instant timestamp,
        String traceId,
        String path,
        List<ValidationError> errors,
        Map<String, Object> details
) {

    /**
     * 功能：基于错误码快速构造统一错误响应。
     *
     * @param responseCode 统一错误码
     * @param message      错误说明
     * @param path         请求路径
     * @param errors       字段级校验错误
     * @param details      扩展错误信息
     * @return 统一错误响应
     */
    public static ApiErrorResponse of(
            ResponseCode responseCode,
            String message,
            String path,
            List<ValidationError> errors,
            Map<String, Object> details
    ) {
        return new ApiErrorResponse(
                false,
                responseCode.code(),
                message,
                ResponseStatus.FAILURE,
                Instant.now(),
                TraceContext.getTraceId(),
                path,
                errors == null ? List.of() : List.copyOf(errors),
                details == null ? Map.of() : Map.copyOf(details)
        );
    }

    /**
     * 创建时间：2026/03/12
     * 功能：描述单个字段的校验错误信息。
     * 作者：Devil
     *
     * @param field   字段名
     * @param message 错误说明
     */
    public record ValidationError(String field, String message) {
    }
}
