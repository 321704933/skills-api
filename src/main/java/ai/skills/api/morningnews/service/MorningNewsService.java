package ai.skills.api.morningnews.service;

import ai.skills.api.common.cache.CacheService;
import ai.skills.api.morningnews.MorningNewsCollector;
import ai.skills.api.morningnews.MorningNewsResult;
import ai.skills.api.morningnews.NewsCategory;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MorningNewsService {

    private static final String CACHE_KEY_PREFIX = "morningnews:";
    private static final String CACHE_KEY_SUFFIX = ":latest";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    private final Map<String, MorningNewsCollector> collectors;
    private final CacheService cacheService;

    /**
     * 功能：将采集结果缓存。
     *
     * @param result 采集结果
     */
    public void cache(MorningNewsResult result) {
        if (result == null || result.items() == null || result.items().isEmpty()) {
            log.warn("采集结果为空，跳过缓存");
            return;
        }

        String cacheKey = CACHE_KEY_PREFIX + result.category() + CACHE_KEY_SUFFIX;
        cacheService.set(cacheKey, result, CACHE_TTL);
        log.info("分类 [{}] 早报数据已缓存，共 {} 条，TTL {} 小时",
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
        String cacheKey = CACHE_KEY_PREFIX + category.getCode() + CACHE_KEY_SUFFIX;
        try {
            MorningNewsResult result = cacheService.get(cacheKey);
            if (result != null) {
                return result;
            }

            log.info("分类 [{}] 缓存为空，触发即时采集", category.getCode());
            return collectAndCache(category);
        } catch (Exception e) {
            log.warn("缓存反序列化失败，清理旧缓存，键名：{}", cacheKey);
            cacheService.delete(cacheKey);
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
