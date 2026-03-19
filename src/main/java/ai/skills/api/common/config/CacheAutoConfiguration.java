package ai.skills.api.common.config;

import ai.skills.api.common.cache.CacheService;
import ai.skills.api.common.cache.LocalCacheService;
import ai.skills.api.common.cache.RedisCacheService;
import ai.skills.api.common.idempotency.IdempotencyStore;
import ai.skills.api.common.idempotency.LocalIdempotencyStore;
import ai.skills.api.common.ratelimit.LocalRateLimiter;
import ai.skills.api.common.ratelimit.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PreDestroy;

/**
 * 创建时间：2026/03/19
 * 功能：缓存自动配置类，根据配置选择 Redis 或本地缓存实现。
 * 作者：Devil
 */
@AutoConfiguration
@Slf4j
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    private final CacheProperties cacheProperties;
    private LocalCacheService localCacheService;

    public CacheAutoConfiguration(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    // ==================== CacheService Bean ====================

    /**
     * 功能：创建本地缓存服务。
     * 触发条件：skills-api.cache.type=local
     */
    @Bean
    @ConditionalOnMissingBean(CacheService.class)
    @ConditionalOnProperty(prefix = "skills-api.cache", name = "type", havingValue = "local", matchIfMissing = true)
    public CacheService localCacheService() {
        if (localCacheService == null) {
            CacheProperties.LocalCacheConfig config = cacheProperties.getLocal();
            localCacheService = new LocalCacheService(
                config.getMaxSize(),
                config.getCleanupInterval()
            );
            log.info("本地缓存服务已创建，最大条目数：{}，清理间隔：{}",
                    config.getMaxSize(), config.getCleanupInterval());
        }
        return localCacheService;
    }

    /**
     * 功能：创建 Redis 缓存服务。
     * 触发条件：skills-api.cache.type=redis
     */
    @Bean
    @ConditionalOnMissingBean(CacheService.class)
    @ConditionalOnProperty(prefix = "skills-api.cache", name = "type", havingValue = "redis")
    public CacheService redisCacheService() {
        log.info("Redis 缓存服务已创建");
        return new RedisCacheService();
    }

    // ==================== IdempotencyStore Bean ====================

    /**
     * 功能：创建本地幂等存储。
     * 触发条件：skills-api.cache.type=local
     */
    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    @ConditionalOnProperty(prefix = "skills-api.cache", name = "type", havingValue = "local", matchIfMissing = true)
    public IdempotencyStore localIdempotencyStore(CacheService cacheService) {
        log.info("本地幂等存储已创建");
        return new LocalIdempotencyStore(cacheService);
    }

    // ==================== RateLimiter Bean ====================

    /**
     * 功能：创建本地限流器。
     * 触发条件：skills-api.cache.type=local
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = "skills-api.cache", name = "type", havingValue = "local", matchIfMissing = true)
    public RateLimiter localRateLimiter() {
        log.info("本地限流器已创建");
        return new LocalRateLimiter();
    }

    /**
     * 功能：销毁本地缓存服务（如果存在）。
     */
    @PreDestroy
    public void destroy() {
        if (localCacheService != null) {
            localCacheService.shutdown();
            log.info("本地缓存服务已销毁");
        }
    }
}
