package ai.skills.api.common.ratelimit;

import java.time.Duration;

/**
 * 创建时间：2026/03/12
 * 功能：描述单条限流规则。
 * 作者：Devil
 *
 * @param permits 窗口内允许的请求次数
 * @param window  窗口时长
 */
public record RateLimitRule(int permits, Duration window) {
}
