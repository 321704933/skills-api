package ai.skills.api.common.cache;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 创建时间：2026/03/19
 * 功能：本地内存缓存实现，支持 TTL 过期和自动清理。
 * 说明：适用于无 Redis 环境的单机部署场景，不支持分布式。
 * 作者：Devil
 */
@Slf4j
public class LocalCacheService implements CacheService {

    /** 缓存存储 */
    private final ConcurrentHashMap<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    /** 计数器存储 */
    private final ConcurrentHashMap<String, Long> counters = new ConcurrentHashMap<>();

    /** 过期清理调度器 */
    private final ScheduledExecutorService cleanupScheduler;

    /** 最大缓存条目数 */
    private final int maxSize;

    /** 清理间隔 */
    private final Duration cleanupInterval;

    /**
     * 构造函数
     *
     * @param maxSize        最大缓存条目数（默认 10000）
     * @param cleanupInterval 清理间隔（默认 1 分钟）
     */
    public LocalCacheService(int maxSize, Duration cleanupInterval) {
        this.maxSize = maxSize > 0 ? maxSize : 10000;
        this.cleanupInterval = cleanupInterval != null ? cleanupInterval : Duration.ofMinutes(1);
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "local-cache-cleanup");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 默认构造函数（使用默认配置）
     */
    public LocalCacheService() {
        this(10000, Duration.ofMinutes(1));
    }

    /**
     * 启动时开始定期清理过期缓存
     */
    @PostConstruct
    public void startCleanup() {
        long intervalMillis = cleanupInterval.toMillis();
        cleanupScheduler.scheduleAtFixedRate(
            this::cleanupExpiredEntries,
            intervalMillis,
            intervalMillis,
            TimeUnit.MILLISECONDS
        );
        log.info("本地缓存服务已启动，最大条目数：{}，清理间隔：{}ms", maxSize, intervalMillis);
    }

    /**
     * 销毁时停止清理线程
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
        log.info("本地缓存服务已关闭");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry<?> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        return (T) entry.getValue();
    }

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("缓存键和值不能为空");
        }
        // 检查是否超过最大条目数
        if (cache.size() >= maxSize && !cache.containsKey(key)) {
            // 触发一次清理，尝试释放空间
            cleanupExpiredEntries();
            if (cache.size() >= maxSize) {
                log.warn("本地缓存已满（当前：{}），无法添加新键：{}", cache.size(), key);
                return;
            }
        }
        Instant expireAt = ttl != null ? Instant.now().plus(ttl) : null;
        cache.put(key, new CacheEntry<>(value, expireAt));
    }

    @Override
    public <T> boolean setIfAbsent(String key, T value, Duration ttl) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("缓存键和值不能为空");
        }
        // 先检查是否存在且未过期
        CacheEntry<?> existing = cache.get(key);
        if (existing != null && !existing.isExpired()) {
            return false;
        }
        // 不存在或已过期，设置新值
        Instant expireAt = ttl != null ? Instant.now().plus(ttl) : null;
        cache.put(key, new CacheEntry<>(value, expireAt));
        return true;
    }

    @Override
    public boolean delete(String key) {
        return cache.remove(key) != null;
    }

    @Override
    public boolean exists(String key) {
        CacheEntry<?> entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        return this.get(key);
    }

    @Override
    public <T> void setList(String key, List<T> list, Duration ttl) {
        this.set(key, list, ttl);
    }

    @Override
    public long increment(String key) {
        return counters.compute(key, (k, v) -> v == null ? 1L : v + 1);
    }

    @Override
    public long decrement(String key) {
        return counters.compute(key, (k, v) -> v == null ? -1L : v - 1);
    }

    @Override
    public long getCounter(String key) {
        return counters.getOrDefault(key, 0L);
    }

    /**
     * 功能：清理过期的缓存条目。
     */
    private void cleanupExpiredEntries() {
        int removed = 0;
        for (Map.Entry<String, CacheEntry<?>> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                if (cache.remove(entry.getKey()) != null) {
                    removed++;
                }
            }
        }
        if (removed > 0) {
            log.debug("本地缓存清理完成，移除过期条目：{} 条，当前条目数：{}", removed, cache.size());
        }
    }

    /**
     * 功能：获取当前缓存条目数（用于监控）。
     *
     * @return 当前缓存条目数
     */
    public int size() {
        return cache.size();
    }

    /**
     * 缓存条目内部类
     */
    @Getter
    private static class CacheEntry<T> {
        private final T value;
        private final Instant expireAt;

        public CacheEntry(T value, Instant expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        public boolean isExpired() {
            return expireAt != null && Instant.now().isAfter(expireAt);
        }
    }
}
