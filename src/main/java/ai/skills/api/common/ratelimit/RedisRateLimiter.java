package ai.skills.api.common.ratelimit;

import ai.skills.api.common.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.Instant;

/**
 * 创建时间：2026/03/12
 * 功能：基于 Redis（Redisson）实现的滑动窗口限流器。
 * 作者：Devil
 */
@Slf4j
public class RedisRateLimiter implements RateLimiter {

    private static final String KEY_PREFIX = "ratelimit:";

    private final RedissonClient redissonClient;

    public RedisRateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 功能：根据给定规则执行一次限流判定（滑动窗口算法）。
     *
     * @param key  限流键
     * @param rule 限流规则
     * @param now  当前时间
     * @return 限流判定结果
     */
    @Override
    public RateLimitDecision acquire(String key, RateLimitRule rule, Instant now) {
        String redisKey = KEY_PREFIX + key;
        long windowSeconds = rule.window().getSeconds();
        int permits = rule.permits();

        try {
            // 使用 Redisson 的限流器
            long result = RedisUtils.rateLimiter(redisKey, RateType.OVERALL, permits, (int) windowSeconds);

            if (result >= 0) {
                // 成功获取许可
                return new RateLimitDecision(true, (int) result, now.plus(rule.window()));
            } else {
                // 被限流
                return new RateLimitDecision(false, 0, now.plus(rule.window()));
            }
        } catch (Exception e) {
            log.error("限流器操作失败，键名：{}", key, e);
            // 限流失败时放行，避免影响业务
            return new RateLimitDecision(true, rule.permits() - 1, now.plus(rule.window()));
        }
    }

    /**
     * 功能：固定窗口计数限流（备选方案，性能更高但边界问题更明显）。
     *
     * @param key  限流键
     * @param rule 限流规则
     * @param now  当前时间
     * @return 限流判定结果
     */
    public RateLimitDecision acquireFixedWindow(String key, RateLimitRule rule, Instant now) {
        String redisKey = KEY_PREFIX + "fw:" + key;
        long windowSeconds = rule.window().getSeconds();

        try {
            RAtomicLong counter = redissonClient.getAtomicLong(redisKey);
            long current = counter.get();

            if (current == 0) {
                // 首次访问，设置计数和过期时间
                counter.set(1);
                counter.expire(Duration.ofSeconds(windowSeconds));
                return new RateLimitDecision(true, rule.permits() - 1, now.plus(rule.window()));
            }

            if (current >= rule.permits()) {
                // 超过限制
                long ttl = counter.remainTimeToLive();
                Instant resetAt = ttl > 0 ? now.plusMillis(ttl) : now.plus(rule.window());
                return new RateLimitDecision(false, 0, resetAt);
            }

            // 未超过限制，递增计数
            long newCount = counter.incrementAndGet();
            int remaining = (int) Math.max(0, rule.permits() - newCount);
            return new RateLimitDecision(true, remaining, now.plus(rule.window()));

        } catch (Exception e) {
            log.error("固定窗口限流器操作失败，键名：{}", key, e);
            return new RateLimitDecision(true, rule.permits() - 1, now.plus(rule.window()));
        }
    }
}
