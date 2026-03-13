package ai.skills.api.common.api;

import ai.skills.api.common.web.TraceContext;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * 创建时间：2026/03/12
 * 功能：定义 API（应用程序编程接口）成功响应的统一结构。
 * 作者：Devil
 */
@Schema(name = "统一响应", description = "API 统一响应结构")
public record ApiResponse<T>(
        @Schema(description = "是否成功", example = "true")
        boolean success,

        @Schema(description = "业务状态码", example = "200")
        Integer code,

        @Schema(description = "响应说明", example = "success")
        String message,

        @Schema(description = "响应状态枚举")
        ResponseStatus status,

        @Schema(description = "响应时间", example = "2026-03-13T10:00:00")
        Instant timestamp,

        @Schema(description = "链路追踪编号", example = "a1b2c3d4e5f6")
        String traceId,

        @Schema(description = "业务数据")
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
