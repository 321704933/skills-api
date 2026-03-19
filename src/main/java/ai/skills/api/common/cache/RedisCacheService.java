package ai.skills.api.common.cache;

import ai.skills.api.common.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

/**
 * 创建时间：2026/03/19
 * 功能：Redis 缓存实现，委托给 RedisUtils 执行实际操作。
 * 说明：保持与现有 RedisUtils 的兼容性，作为默认缓存实现。
 * 作者：Devil
 */
@Slf4j
public class RedisCacheService implements CacheService {

    @Override
    public <T> T get(String key) {
        return RedisUtils.getCacheObject(key);
    }

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        if (ttl != null) {
            RedisUtils.setCacheObject(key, value, ttl);
        } else {
            RedisUtils.setCacheObject(key, value);
        }
    }

    @Override
    public <T> boolean setIfAbsent(String key, T value, Duration ttl) {
        return RedisUtils.setIfAbsent(key, value, ttl);
    }

    @Override
    public boolean delete(String key) {
        return RedisUtils.deleteObject(key);
    }

    @Override
    public boolean exists(String key) {
        return RedisUtils.isExistsObject(key);
    }

    @Override
    public <T> List<T> getList(String key) {
        return RedisUtils.getCacheList(key);
    }

    @Override
    public <T> void setList(String key, List<T> list, Duration ttl) {
        RedisUtils.setCacheList(key, list);
        if (ttl != null) {
            RedisUtils.expire(key, ttl);
        }
    }

    @Override
    public long increment(String key) {
        return RedisUtils.incrAtomicValue(key);
    }

    @Override
    public long decrement(String key) {
        return RedisUtils.decrementAndGet(key);
    }

    @Override
    public long getCounter(String key) {
        return RedisUtils.getAtomicValue(key);
    }
}
