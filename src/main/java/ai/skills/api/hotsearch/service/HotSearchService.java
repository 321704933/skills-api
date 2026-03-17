package ai.skills.api.hotsearch.service;

import ai.skills.api.common.redis.RedisUtils;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

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
     *
     * @param platform 平台枚举
     * @return 热搜结果（无数据返回 null）
     */
    public HotSearchResult getLatest(Platform platform) {
        String redisKey = REDIS_KEY_PREFIX + platform.getCode() + REDIS_KEY_SUFFIX;
        try {
            return RedisUtils.getCacheObject(redisKey);
        } catch (Exception e) {
            log.warn("Redis 缓存反序列化失败，清理旧缓存，键名：{}", redisKey);
            RedisUtils.deleteObject(redisKey);
            return null;
        }
    }
}
