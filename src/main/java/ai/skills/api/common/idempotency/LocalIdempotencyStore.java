package ai.skills.api.common.idempotency;

import ai.skills.api.common.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

/**
 * 创建时间：2026/03/19
 * 功能：本地幂等存储实现，基于 CacheService 存储幂等状态。
 * 说明：适用于无 Redis 环境的单机部署场景，不支持分布式。
 * 作者：Devil
 */
@Slf4j
@RequiredArgsConstructor
public class LocalIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "idempotent:";

    /** 幂等状态枚举 */
    private enum Status {
        PROCESSING,
        COMPLETED
    }

    /** 缓存服务 */
    private final CacheService cacheService;

    @Override
    public boolean tryStart(String key, Duration ttl, Instant now) {
        String fullKey = KEY_PREFIX + key;
        // 使用 SETNX 语义占用键
        return cacheService.setIfAbsent(fullKey, Status.PROCESSING.name(), ttl);
    }

    @Override
    public void markCompleted(String key, Duration ttl, Instant now) {
        String fullKey = KEY_PREFIX + key;
        Status current = cacheService.get(fullKey);
        if (current != null) {
            cacheService.set(fullKey, Status.COMPLETED, ttl);
        }
    }

    @Override
    public void release(String key) {
        String fullKey = KEY_PREFIX + key;
        cacheService.delete(fullKey);
    }
}
