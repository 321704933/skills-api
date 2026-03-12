package ai.skills.api.common.idempotency;

import ai.skills.api.common.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * 创建时间：2026/03/12
 * 功能：基于 Redis（Redisson）实现幂等存储，支持分布式环境。
 * 作者：Devil
 */
@Slf4j
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "idempotent:";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";

    /**
     * 功能：尝试占用一个幂等键（SETNX 语义）。
     *
     * @param key 幂等键
     * @param ttl TTL（Time To Live，生存时间）
     * @param now 当前时间
     * @return 是否占用成功
     */
    @Override
    public boolean tryStart(String key, Duration ttl, Instant now) {
        String redisKey = KEY_PREFIX + key;
        try {
            return RedisUtils.setIfAbsent(redisKey, STATUS_PROCESSING, ttl);
        } catch (Exception e) {
            log.error("幂等键占用失败，键名：{}", key, e);
            return false;
        }
    }

    /**
     * 功能：把幂等键标记为已完成状态。
     *
     * @param key 幂等键
     * @param ttl TTL（Time To Live，生存时间）
     * @param now 当前时间
     */
    @Override
    public void markCompleted(String key, Duration ttl, Instant now) {
        String redisKey = KEY_PREFIX + key;
        try {
            String currentValue = RedisUtils.getCacheObject(redisKey);
            if (currentValue != null) {
                RedisUtils.setCacheObject(redisKey, STATUS_COMPLETED, ttl);
            }
        } catch (Exception e) {
            log.error("幂等键标记完成失败，键名：{}", key, e);
        }
    }

    /**
     * 功能：释放幂等键占位。
     *
     * @param key 幂等键
     */
    @Override
    public void release(String key) {
        String redisKey = KEY_PREFIX + key;
        try {
            RedisUtils.deleteObject(redisKey);
        } catch (Exception e) {
            log.error("幂等键释放失败，键名：{}", key, e);
        }
    }

    /**
     * 功能：检查幂等键是否存在且状态为已完成。
     *
     * @param key 幂等键
     * @return 状态（可能为空）
     */
    public Optional<String> getStatus(String key) {
        String redisKey = KEY_PREFIX + key;
        try {
            String value = RedisUtils.getCacheObject(redisKey);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("获取幂等键状态失败，键名：{}", key, e);
            return Optional.empty();
        }
    }

    /**
     * 功能：检查幂等键是否正在处理中。
     *
     * @param key 幂等键
     * @return 是否处理中
     */
    public boolean isProcessing(String key) {
        return getStatus(key)
                .map(STATUS_PROCESSING::equals)
                .orElse(false);
    }

    /**
     * 功能：检查幂等键是否已完成。
     *
     * @param key 幂等键
     * @return 是否已完成
     */
    public boolean isCompleted(String key) {
        return getStatus(key)
                .map(STATUS_COMPLETED::equals)
                .orElse(false);
    }
}
