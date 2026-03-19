package ai.skills.api.common.cache;

import java.time.Duration;
import java.util.List;

/**
 * 创建时间：2026/03/19
 * 功能：缓存服务抽象接口，支持 Redis 和本地内存两种实现。
 * 说明：提供统一的缓存操作 API，业务模块应使用此接口而非直接调用 RedisUtils。
 * 作者：Devil
 */
public interface CacheService {

    /**
     * 功能：获取缓存值。
     *
     * @param key 缓存键
     * @param <T> 值类型
     * @return 缓存值（不存在或已过期则返回 null）
     */
    <T> T get(String key);

    /**
     * 功能：设置缓存值（带 TTL）。
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间
     * @param <T>   值类型
     */
    <T> void set(String key, T value, Duration ttl);

    /**
     * 功能：仅当键不存在时设置值（SETNX 语义）。
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间
     * @param <T>   值类型
     * @return 是否设置成功
     */
    <T> boolean setIfAbsent(String key, T value, Duration ttl);

    /**
     * 功能：删除缓存键。
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 功能：检查缓存键是否存在。
     *
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 功能：获取缓存的列表。
     *
     * @param key 缓存键
     * @param <T> 列表元素类型
     * @return 列表数据（不存在则返回 null）
     */
    <T> List<T> getList(String key);

    /**
     * 功能：设置缓存的列表（带 TTL）。
     *
     * @param key  缓存键
     * @param list 列表数据
     * @param ttl  过期时间
     * @param <T>  列表元素类型
     */
    <T> void setList(String key, List<T> list, Duration ttl);

    /**
     * 功能：原子递增操作。
     *
     * @param key 缓存键
     * @return 递增后的值
     */
    long increment(String key);

    /**
     * 功能：原子递减操作。
     *
     * @param key 缓存键
     * @return 递减后的值
     */
    long decrement(String key);

    /**
     * 功能：获取计数器当前值。
     *
     * @param key 缓存键
     * @return 当前计数值（不存在则返回 0）
     */
    long getCounter(String key);
}
