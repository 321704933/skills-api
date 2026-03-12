package ai.skills.api.common.ratelimit;

import java.time.Instant;

/**
 * 创建时间：2026/03/12
 * 功能：定义限流器抽象，便于后续扩展不同存储实现。
 * 作者：Devil
 */
public interface RateLimiter {

    /**
     * 功能：根据给定规则执行一次限流判定。
     *
     * @param key  限流键
     * @param rule 限流规则
     * @param now  当前时间
     * @return 限流判定结果
     */
    RateLimitDecision acquire(String key, RateLimitRule rule, Instant now);
}
