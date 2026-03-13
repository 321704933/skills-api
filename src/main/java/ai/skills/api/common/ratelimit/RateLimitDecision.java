package ai.skills.api.common.ratelimit;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * 创建时间：2026/03/12
 * 功能：描述一次限流判定结果。
 * 作者：Devil
 */
@Schema(name = "限流判定结果", description = "限流判定结果详情")
public record RateLimitDecision(
        @Schema(description = "是否允许通过", example = "true")
        boolean allowed,

        @Schema(description = "剩余可用次数", example = "59")
        int remaining,

        @Schema(description = "限流窗口重置时间", example = "2026-03-13T10:01:00")
        Instant resetAt) {
}
