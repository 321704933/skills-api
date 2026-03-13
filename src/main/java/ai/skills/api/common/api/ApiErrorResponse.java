package ai.skills.api.common.api;

import ai.skills.api.common.web.TraceContext;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 创建时间：2026/03/12
 * 功能：定义 API（应用程序编程接口）失败响应的统一结构。
 * 作者：Devil
 */
@Schema(name = "错误响应", description = "API 错误响应结构")
public record ApiErrorResponse(
        @Schema(description = "是否成功", example = "false")
        boolean success,

        @Schema(description = "错误码", example = "400")
        Integer code,

        @Schema(description = "错误说明", example = "参数错误")
        String message,

        @Schema(description = "响应状态枚举")
        ResponseStatus status,

        @Schema(description = "响应时间", example = "2026-03-13T10:00:00")
        Instant timestamp,

        @Schema(description = "链路追踪编号", example = "a1b2c3d4e5f6")
        String traceId,

        @Schema(description = "请求路径", example = "/api/v1/demo/ping")
        String path,

        @Schema(description = "字段级校验错误列表")
        List<ValidationError> errors,

        @Schema(description = "扩展错误明细")
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
     */
    @Schema(name = "字段校验错误", description = "单个字段的校验错误信息")
    public record ValidationError(
            @Schema(description = "字段名", example = "username")
            String field,

            @Schema(description = "错误说明", example = "用户名不能为空")
            String message) {
    }
}
