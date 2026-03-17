package ai.skills.api.hotsearch.service;

import ai.skills.api.common.redis.RedisUtils;
import ai.skills.api.hotsearch.HotSearchCollector;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 * 创建时间：2026/03/13
 * 功能：热搜数据服务层，负责采集结果的缓存和查询。
 * 作者：Devil
 */
@Slf4j
@Service
public class HotSearchService {

    private static final String REDIS_KEY_PREFIX = "hotsearch:";
    private static final String REDIS_KEY_SUFFIX = ":latest";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    private final Map<String, HotSearchCollector> collectors;

    public HotSearchService(Map<String, HotSearchCollector> collectors) {
        this.collectors = collectors;
    }

    /**
     * 功能：将采集结果缓存到 Redis。
     *
     * @param result 采集结果
     */
    public void cache(HotSearchResult result) {
        if (result == null || result.items() == null || result.items().isEmpty()) {
            log.warn("采集结果为空，跳过缓存");
            return;
        }

        String redisKey = REDIS_KEY_PREFIX + result.platform() + REDIS_KEY_SUFFIX;
        RedisUtils.setCacheObject(redisKey, result, CACHE_TTL);
        log.info("平台 [{}] 热搜数据已缓存至 Redis，共 {} 条，TTL {} 小时",
                result.platform(), result.items().size(), CACHE_TTL.toHours());
    }

    /**
     * 功能：获取指定平台的最新一批热搜数据。
     * <p>
     * 若缓存为空，则自动触发一次采集。
     *
     * @param platform 平台枚举
     * @return 热搜结果（无数据返回 null）
     */
    public HotSearchResult getLatest(Platform platform) {
        String redisKey = REDIS_KEY_PREFIX + platform.getCode() + REDIS_KEY_SUFFIX;
        try {
            HotSearchResult result = RedisUtils.getCacheObject(redisKey);
            if (result != null) {
                return result;
            }

            log.info("平台 [{}] 缓存为空，触发即时采集", platform.getCode());
            return collectAndCache(platform);
        } catch (Exception e) {
            log.warn("Redis 缓存反序列化失败，清理旧缓存，键名：{}", redisKey);
            RedisUtils.deleteObject(redisKey);
            return collectAndCache(platform);
        }
    }

    /**
     * 功能：执行即时采集并缓存结果。
     *
     * @param platform 平台枚举
     * @return 采集结果（失败返回 null）
     */
    private HotSearchResult collectAndCache(Platform platform) {
        HotSearchCollector collector = collectors.get(platform.getCode());
        if (collector == null) {
            log.warn("平台 [{}] 未找到对应的采集器", platform.getCode());
            return null;
        }

        try {
            HotSearchResult result = collector.collect();
            cache(result);
            return result;
        } catch (Exception e) {
            log.error("平台 [{}] 即时采集失败", platform.getCode(), e);
            return null;
        }
    }
}
