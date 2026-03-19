package ai.skills.api.common.ratelimit;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 创建时间：2026/03/19
 * 功能：本地限流器实现，基于滑动窗口算法。
 * 说明：适用于无 Redis 环境的单机部署场景，不支持分布式限流。
 * 作者：Devil
 */
@Slf4j
public class LocalRateLimiter implements RateLimiter {

    private static final String KEY_PREFIX = "ratelimit:";

    /** 滑动窗口存储：键 -> 请求时间戳队列 */
    private final ConcurrentHashMap<String, Deque<Instant>> windows = new ConcurrentHashMap<>();

    @Override
    public RateLimitDecision acquire(String key, RateLimitRule rule, Instant now) {
        String rateKey = KEY_PREFIX + key;
        Deque<Instant> timestamps = windows.computeIfAbsent(rateKey, k -> new ConcurrentLinkedDeque<>());

        // 计算窗口边界
        Instant windowStart = now.minus(rule.window());

        // 清理窗口外的时间戳
        timestamps.removeIf(ts -> ts.isBefore(windowStart));

        // 检查是否允许通过
        if (timestamps.size() < rule.permits()) {
            timestamps.addLast(now);
            int remaining = rule.permits() - timestamps.size();
            return new RateLimitDecision(true, remaining, now.plus(rule.window()));
        }

        // 计算重置时间（最早过期时间戳 + 窗口大小）
        Instant oldest = timestamps.peekFirst();
        Instant resetAt = oldest != null ? oldest.plus(rule.window()) : now.plus(rule.window());
        return new RateLimitDecision(false, 0, resetAt);
    }
}
