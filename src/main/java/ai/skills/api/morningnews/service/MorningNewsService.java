package ai.skills.api.morningnews.service;

import ai.skills.api.common.redis.RedisUtils;
import ai.skills.api.morningnews.MorningNewsCollector;
import ai.skills.api.morningnews.MorningNewsResult;
import ai.skills.api.morningnews.NewsCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 * 创建时间：2026/03/17
 * 功能：早报数据服务层，负责采集结果的缓存和查询。
 * 作者：Devil
 */
@Slf4j
@Service
public class MorningNewsService {

    private static final String REDIS_KEY_PREFIX = "morningnews:";
    private static final String REDIS_KEY_SUFFIX = ":latest";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    private final Map<String, MorningNewsCollector> collectors;

    public MorningNewsService(Map<String, MorningNewsCollector> collectors) {
        this.collectors = collectors;
    }

    /**
     * 功能：将采集结果缓存到 Redis。
     *
     * @param result 采集结果
     */
    public void cache(MorningNewsResult result) {
        if (result == null || result.items() == null || result.items().isEmpty()) {
            log.warn("采集结果为空，跳过缓存");
            return;
        }

        String redisKey = REDIS_KEY_PREFIX + result.category() + REDIS_KEY_SUFFIX;
        RedisUtils.setCacheObject(redisKey, result, CACHE_TTL);
        log.info("分类 [{}] 早报数据已缓存至 Redis，共 {} 条，TTL {} 小时",
                result.category(), result.items().size(), CACHE_TTL.toHours());
    }

    /**
     * 功能：获取指定分类的最新一批早报数据。
     * <p>
     * 若缓存为空，则自动触发一次采集。
     *
     * @param category 分类枚举
     * @return 早报结果（无数据返回 null）
     */
    public MorningNewsResult getLatest(NewsCategory category) {
        String redisKey = REDIS_KEY_PREFIX + category.getCode() + REDIS_KEY_SUFFIX;
        try {
            MorningNewsResult result = RedisUtils.getCacheObject(redisKey);
            if (result != null) {
                return result;
            }

            log.info("分类 [{}] 缓存为空，触发即时采集", category.getCode());
            return collectAndCache(category);
        } catch (Exception e) {
            log.warn("Redis 缓存反序列化失败，清理旧缓存，键名：{}", redisKey);
            RedisUtils.deleteObject(redisKey);
            return collectAndCache(category);
        }
    }

    /**
     * 功能：执行即时采集并缓存结果。
     *
     * @param category 分类枚举
     * @return 采集结果（失败返回 null）
     */
    private MorningNewsResult collectAndCache(NewsCategory category) {
        MorningNewsCollector collector = collectors.get(category.getCode());
        if (collector == null) {
            log.warn("分类 [{}] 未找到对应的采集器", category.getCode());
            return null;
        }

        try {
            MorningNewsResult result = collector.collect();
            cache(result);
            return result;
        } catch (Exception e) {
            log.error("分类 [{}] 即时采集失败", category.getCode(), e);
            return null;
        }
    }
}
