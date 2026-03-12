package ai.skills.api.common.ratelimit;

import java.time.Instant;

/**
 * 创建时间：2026/03/12
 * 功能：描述一次限流判定结果。
 * 作者：Devil
 *
 * @param allowed   是否允许通过
 * @param remaining 剩余可用次数
 * @param resetAt   限流窗口重置时间
 */
public record RateLimitDecision(boolean allowed, int remaining, Instant resetAt) {
}
