package ai.skills.api.common.ratelimit;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;

/**
 * 创建时间：2026/03/12
 * 功能：描述单条限流规则。
 * 作者：Devil
 */
@Schema(name = "限流规则", description = "限流规则详情")
public record RateLimitRule(
        @Schema(description = "窗口内允许的请求次数", example = "60")
        int permits,

        @Schema(description = "窗口时长", example = "PT1M")
        Duration window) {
}
