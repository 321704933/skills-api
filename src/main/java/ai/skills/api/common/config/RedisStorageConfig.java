package ai.skills.api.common.config;

import ai.skills.api.common.idempotency.IdempotencyStore;
import ai.skills.api.common.idempotency.RedisIdempotencyStore;
import ai.skills.api.common.ratelimit.RateLimiter;
import ai.skills.api.common.ratelimit.RedisRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 创建时间：2026/03/12
 * 功能：Redis 存储配置类，注册限流器和幂等存储 Bean。
 * 作者：Devil
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "skills-api.cache", name = "type", havingValue = "redis")
public class RedisStorageConfig {

    /**
     * 功能：创建基于 Redis 的限流器。
     *
     * @param redissonClient Redisson 客户端
     * @return Redis 限流器
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter rateLimiter(RedissonClient redissonClient) {
        return new RedisRateLimiter(redissonClient);
    }

    /**
     * 功能：创建基于 Redis 的幂等存储。
     *
     * @return Redis 幂等存储
     */
    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    public IdempotencyStore idempotencyStore() {
        return new RedisIdempotencyStore();
    }
}
