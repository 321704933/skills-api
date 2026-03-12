package ai.skills.api.common.api;

import ai.skills.api.common.web.TraceContext;

import java.time.Instant;

/**
 * 创建时间：2026/03/12
 * 功能：定义 API（应用程序编程接口）成功响应的统一结构。
 * 作者：Devil
 *
 * @param success   是否成功
 * @param code      业务状态码
 * @param message   响应说明
 * @param status    响应状态枚举
 * @param timestamp 响应时间
 * @param traceId   traceId（链路追踪编号）
 * @param data      业务数据
 * @param <T>       数据泛型类型
 */
public record ApiResponse<T>(
        boolean success,
        Integer code,
        String message,
        ResponseStatus status,
        Instant timestamp,
        String traceId,
        T data
) {

    /**
     * 功能：快速构造标准成功响应。
     *
     * @param data 业务数据
     * @param <T>  数据泛型类型
     * @return 统一成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                ResponseCode.SUCCESS.code(),
                ResponseCode.SUCCESS.defaultMessage(),
                ResponseStatus.SUCCESS,
                Instant.now(),
                TraceContext.getTraceId(),
                data
        );
    }
}
