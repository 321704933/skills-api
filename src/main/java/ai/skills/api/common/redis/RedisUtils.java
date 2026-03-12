package ai.skills.api.common.redis;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.api.options.KeysScanOptions;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 创建时间：2026/03/12
 * 功能：Redis 工具类（基于 Redisson），提供分布式锁、限流、缓存等常用操作封装。
 * 说明：序列化配置已在 {@link ai.skills.api.common.config.RedissonConfig} 中统一配置。
 * 作者：Devil
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisUtils {

    private static final RedissonClient CLIENT = SpringUtil.getBean(RedissonClient.class);

    /* ==================== 分布式锁操作 ==================== */

    /**
     * 功能：执行带锁的操作（自动释放锁）。
     *
     * @param lockKey   锁键名
     * @param waitTime  等待获取锁的时间（秒）
     * @param leaseTime 锁自动释放时间（秒），-1 表示自动续期
     * @param action    需要在锁内执行的业务逻辑
     */
    public static void executeWithLock(String lockKey, long waitTime, long leaseTime, Runnable action) {
        RLock lock = getLock(lockKey);
        try {
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                action.run();
            } else {
                log.error("Redis 分布式锁获取超时，键名：{}", lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Redis 分布式锁获取被中断，键名：{}", lockKey, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 功能：快速执行带锁操作（默认等待 3 秒，自动续期）。
     *
     * @param lockKey 锁键名
     * @param action  需要在锁内执行的业务逻辑
     */
    public static void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(lockKey, 3, -1, action);
    }

    /**
     * 功能：获取分布式锁对象。
     *
     * @param lockKey 锁键名
     * @return RLock 锁对象
     */
    public static RLock getLock(String lockKey) {
        return CLIENT.getLock(lockKey);
    }

    /**
     * 功能：尝试获取锁（立即返回结果）。
     *
     * @param lockKey 锁键名
     * @return 是否获取成功
     */
    public static boolean tryLock(String lockKey) {
        RLock lock = getLock(lockKey);
        try {
            return lock.tryLock();
        } catch (Exception e) {
            log.error("Redis 尝试获取锁失败，键名：{}", lockKey, e);
            return false;
        }
    }

    /**
     * 功能：尝试获取锁（指定等待时间和持有时间）。
     *
     * @param lockKey   锁键名
     * @param waitTime  等待时间（毫秒）
     * @param leaseTime 持有时间（毫秒）
     * @return 是否获取成功
     */
    public static boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        RLock lock = getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Redis 获取锁被中断，键名：{}", lockKey, e);
            return false;
        } catch (Exception e) {
            log.error("Redis 获取锁失败，键名：{}", lockKey, e);
            return false;
        }
    }

    /**
     * 功能：释放分布式锁。
     *
     * @param lockKey 锁键名
     */
    public static void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.error("Redis 释放锁失败：当前线程未持有锁，键名：{}", lockKey);
            }
        }
    }

    /**
     * 功能：强制释放分布式锁（慎用，可能影响其他线程）。
     *
     * @param lockKey 锁键名
     */
    public static void forceUnlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isLocked()) {
            lock.forceUnlock();
        }
    }

    /**
     * 功能：检查锁是否被任意线程持有。
     *
     * @param lockKey 锁键名
     * @return 是否被锁定
     */
    public static boolean isLocked(String lockKey) {
        return getLock(lockKey).isLocked();
    }

    /* ==================== 限流操作 ==================== */

    /**
     * 功能：限流操作。
     *
     * @param key          限流键名
     * @param rateType     限流类型（全局限流或单机限流）
     * @param rate         速率（单位时间内允许的请求数）
     * @param rateInterval 速率间隔（秒）
     * @return 剩余许可数（-1 表示被限流）
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval) {
        return rateLimiter(key, rateType, rate, rateInterval, 0);
    }

    /**
     * 功能：带超时时间的限流操作。
     *
     * @param key          限流键名
     * @param rateType     限流类型
     * @param rate         速率
     * @param rateInterval 速率间隔（秒）
     * @param timeout      超时时间（秒）
     * @return 剩余许可数（-1 表示被限流）
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval, int timeout) {
        validateKey(key);
        RRateLimiter rateLimiter = CLIENT.getRateLimiter(key);
        try {
            rateLimiter.trySetRate(rateType, rate, Duration.ofSeconds(rateInterval), Duration.ofSeconds(timeout));
            if (rateLimiter.tryAcquire()) {
                return rateLimiter.availablePermits();
            }
        } catch (Exception e) {
            log.error("Redis 限流器操作失败，键名：{}", key, e);
        }
        return -1L;
    }

    /* ==================== 发布订阅操作 ==================== */

    /**
     * 功能：发布消息到指定频道（带回调）。
     *
     * @param channelKey 频道键名
     * @param msg        消息内容
     * @param consumer   发布成功后的回调
     */
    public static <T> void publish(String channelKey, T msg, Consumer<T> consumer) {
        validateKey(channelKey);
        try {
            RTopic topic = CLIENT.getTopic(channelKey);
            long receivers = topic.publish(msg);
            log.debug("消息已发布到 {} 个订阅者，频道：{}", receivers, channelKey);
            consumer.accept(msg);
        } catch (Exception e) {
            log.error("Redis 发布消息失败，频道：{}", channelKey, e);
        }
    }

    /**
     * 功能：发布消息到指定频道。
     *
     * @param channelKey 频道键名
     * @param msg        消息内容
     */
    public static <T> void publish(String channelKey, T msg) {
        publish(channelKey, msg, t -> {});
    }

    /**
     * 功能：订阅指定频道的消息。
     *
     * @param channelKey 频道键名
     * @param clazz      消息类型
     * @param consumer   消息消费回调
     */
    public static <T> void subscribe(String channelKey, Class<T> clazz, Consumer<T> consumer) {
        validateKey(channelKey);
        RTopic topic = CLIENT.getTopic(channelKey);
        topic.addListener(clazz, (channel, msg) -> {
            log.debug("收到频道消息，频道：{}", channel);
            consumer.accept(msg);
        });
    }

    /* ==================== 键值操作 ==================== */

    /**
     * 功能：缓存基本对象（永久有效）。
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public static <T> void setCacheObject(String key, T value) {
        setCacheObject(key, value, false, null);
    }

    /**
     * 功能：缓存基本对象（保留原有 TTL）。
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param keepTTL 是否保留原有 TTL
     */
    public static <T> void setCacheObject(String key, T value, boolean keepTTL) {
        setCacheObject(key, value, keepTTL, null);
    }

    /**
     * 功能：缓存基本对象（指定有效期）。
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param duration 有效期
     */
    public static <T> void setCacheObject(String key, T value, Duration duration) {
        setCacheObject(key, value, false, duration);
    }

    /**
     * 功能：缓存基本对象（内部实现）。
     */
    private static <T> void setCacheObject(String key, T value, boolean keepTTL, Duration duration) {
        validateKeyValue(key, value);
        RBucket<T> bucket = CLIENT.getBucket(key);
        try {
            if (keepTTL) {
                bucket.setAndKeepTTL(value);
            } else if (duration != null) {
                bucket.set(value, duration);
            } else {
                bucket.set(value);
            }
        } catch (Exception e) {
            log.error("Redis 设置缓存失败，键名：{}", key, e);
        }
    }

    /**
     * 功能：获取缓存对象。
     *
     * @param key 缓存键
     * @return 缓存值（不存在则返回 null）
     */
    public static <T> T getCacheObject(String key) {
        validateKey(key);
        try {
            RBucket<T> bucket = CLIENT.getBucket(key);
            return bucket.get();
        } catch (Exception e) {
            log.error("Redis 获取缓存失败，键名：{}", key, e);
            return null;
        }
    }

    /**
     * 功能：仅当键不存在时设置值（SETNX 语义）。
     *
     * @param key   键
     * @param value 值
     * @param ttl   过期时间
     * @return 是否设置成功
     */
    public static <T> boolean setIfAbsent(String key, T value, Duration ttl) {
        validateKeyValue(key, value);
        RBucket<T> bucket = CLIENT.getBucket(key);
        try {
            if (ttl != null) {
                return bucket.setIfAbsent(value, ttl);
            }
            return bucket.setIfAbsent(value);
        } catch (Exception e) {
            log.error("Redis 设置不存在才写入失败，键名：{}", key, e);
            return false;
        }
    }

    /**
     * 功能：设置键的有效期。
     *
     * @param key      键
     * @param duration 有效期
     * @return 是否设置成功
     */
    public static boolean expire(String key, Duration duration) {
        validateKey(key);
        try {
            return CLIENT.getBucket(key).expire(duration);
        } catch (Exception e) {
            log.error("Redis 设置过期时间失败，键名：{}", key, e);
            return false;
        }
    }

    /**
     * 功能：删除单个缓存对象。
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    public static boolean deleteObject(String key) {
        validateKey(key);
        try {
            return CLIENT.getBucket(key).delete();
        } catch (Exception e) {
            log.error("Redis 删除缓存失败，键名：{}", key, e);
            return false;
        }
    }

    /**
     * 功能：批量删除键。
     *
     * @param keys 键列表
     * @return 删除的键数量
     */
    public static long deleteKeys(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0;
        }
        try {
            return CLIENT.getKeys().delete(keys);
        } catch (Exception e) {
            log.error("Redis 批量删除键失败", e);
            return 0;
        }
    }

    /**
     * 功能：检查缓存对象是否存在。
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public static boolean isExistsObject(String key) {
        validateKey(key);
        try {
            return CLIENT.getBucket(key).isExists();
        } catch (Exception e) {
            log.error("Redis 检查键是否存在失败，键名：{}", key, e);
            return false;
        }
    }

    /* ==================== 列表操作 ==================== */

    /**
     * 功能：缓存 List 数据。
     *
     * @param key      缓存键
     * @param dataList 列表数据
     * @return 是否缓存成功
     */
    public static <T> boolean setCacheList(String key, List<T> dataList) {
        validateKeyValue(key, dataList);
        RList<T> rList = CLIENT.getList(key);
        try {
            return rList.addAll(dataList);
        } catch (Exception e) {
            log.error("Redis 缓存列表失败，键名：{}", key, e);
            return false;
        }
    }

    /**
     * 功能：获取缓存的列表。
     *
     * @param key 缓存键
     * @return 列表数据
     */
    public static <T> List<T> getCacheList(String key) {
        validateKey(key);
        RList<T> rList = CLIENT.getList(key);
        try {
            return rList.readAll();
        } catch (Exception e) {
            log.error("Redis 获取列表失败，键名：{}", key, e);
            return null;
        }
    }

    /* ==================== 集合操作 ==================== */

    /**
     * 功能：缓存 Set 数据。
     *
     * @param key     缓存键
     * @param dataSet 集合数据
     * @return 是否缓存成功
     */
    public static <T> boolean setCacheSet(String key, Set<T> dataSet) {
        validateKeyValue(key, dataSet);
        RSet<T> rSet = CLIENT.getSet(key);
        try {
            return rSet.addAll(dataSet);
        } catch (Exception e) {
            log.error("Redis 缓存集合失败，键名：{}", key, e);
            return false;
        }
    }

    /**
     * 功能：获取缓存的集合。
     *
     * @param key 缓存键
     * @return 集合数据
     */
    public static <T> Set<T> getCacheSet(String key) {
        validateKey(key);
        RSet<T> rSet = CLIENT.getSet(key);
        try {
            return rSet.readAll();
        } catch (Exception e) {
            log.error("Redis 获取集合失败，键名：{}", key, e);
            return null;
        }
    }

    /* ==================== 哈希操作 ==================== */

    /**
     * 功能：缓存 Map 数据。
     *
     * @param key     缓存键
     * @param dataMap Map 数据
     */
    public static <T> void setCacheMap(String key, Map<String, T> dataMap) {
        validateKeyValue(key, dataMap);
        RMap<String, T> rMap = CLIENT.getMap(key);
        try {
            rMap.putAll(dataMap);
        } catch (Exception e) {
            log.error("Redis 缓存 Map 失败，键名：{}", key, e);
        }
    }

    /**
     * 功能：获取缓存的 Map。
     *
     * @param key 缓存键
     * @return Map 数据
     */
    public static <T> Map<String, T> getCacheMap(String key) {
        validateKey(key);
        RMap<String, T> rMap = CLIENT.getMap(key);
        try {
            return rMap.getAll(rMap.keySet());
        } catch (Exception e) {
            log.error("Redis 获取 Map 失败，键名：{}", key, e);
            return null;
        }
    }

    /* ==================== 原子操作 ==================== */

    /**
     * 功能：设置原子值。
     *
     * @param key   键
     * @param value 值
     */
    public static void setAtomicValue(String key, long value) {
        validateKey(key);
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        try {
            atomic.set(value);
        } catch (Exception e) {
            log.error("Redis 设置原子值失败，键名：{}", key, e);
        }
    }

    /**
     * 功能：获取原子值。
     *
     * @param key 键
     * @return 原子值
     */
    public static long getAtomicValue(String key) {
        validateKey(key);
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        try {
            return atomic.get();
        } catch (Exception e) {
            log.error("Redis 获取原子值失败，键名：{}", key, e);
            return 0;
        }
    }

    /**
     * 功能：递增原子值。
     *
     * @param key 键
     * @return 递增后的值
     */
    public static long incrAtomicValue(String key) {
        validateKey(key);
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        try {
            return atomic.incrementAndGet();
        } catch (Exception e) {
            log.error("Redis 递增原子值失败，键名：{}", key, e);
            return -1;
        }
    }

    /**
     * 功能：递减原子值。
     *
     * @param key 键
     * @return 递减后的值
     */
    public static long decrementAndGet(String key) {
        validateKey(key);
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        try {
            return atomic.decrementAndGet();
        } catch (Exception e) {
            log.error("Redis 递减原子值失败，键名：{}", key, e);
            return -1;
        }
    }

    /* ==================== 工具方法 ==================== */

    /**
     * 功能：校验键是否有效。
     *
     * @param key 键
     * @throws IllegalArgumentException 键为空时抛出异常
     */
    private static void validateKey(String key) {
        if (StrUtil.isBlank(key)) {
            throw new IllegalArgumentException("Redis 键不能为空");
        }
    }

    /**
     * 功能：校验键和值是否有效。
     *
     * @param key   键
     * @param value 值
     * @throws IllegalArgumentException 键或值无效时抛出异常
     */
    private static <T> void validateKeyValue(String key, T value) {
        validateKey(key);
        if (value == null) {
            throw new IllegalArgumentException("Redis 值不能为空");
        }
    }

    /**
     * 功能：在事务中批量执行操作。
     *
     * @param action 批量操作回调
     */
    public static void executeInTransaction(Consumer<RBatch> action) {
        RBatch batch = CLIENT.createBatch(BatchOptions.defaults().executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC));
        try {
            action.accept(batch);
            batch.execute();
        } catch (Exception e) {
            log.error("Redis 事务执行失败", e);
            batch.discard();
        }
    }

    /**
     * 功能：扫描匹配的键集合。
     *
     * @param keysScanOptions 扫描选项
     * @return 匹配的键集合
     */
    public static Collection<String> keys(KeysScanOptions keysScanOptions) {
        try {
            Stream<String> keysStream = CLIENT.getKeys().getKeysStream(keysScanOptions);
            return keysStream.collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Redis 扫描键失败", e);
            return null;
        }
    }

    /**
     * 功能：获取 Redisson 客户端实例。
     *
     * @return RedissonClient 实例
     */
    public static RedissonClient getClient() {
        return CLIENT;
    }
}
