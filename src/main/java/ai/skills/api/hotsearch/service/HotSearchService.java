package ai.skills.api.hotsearch.service;

import ai.skills.api.common.redis.RedisUtils;
import ai.skills.api.hotsearch.HotSearchItem;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.Platform;
import ai.skills.api.hotsearch.entity.HotSearchRecord;
import ai.skills.api.hotsearch.mapper.HotSearchRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：热搜数据服务层，负责采集结果的持久化、缓存和查询。
 * 作者：Devil
 */
@Slf4j
@Service
public class HotSearchService extends ServiceImpl<HotSearchRecordMapper, HotSearchRecord> {

    private static final String REDIS_KEY_PREFIX = "hotsearch:";
    private static final String REDIS_KEY_SUFFIX = ":latest";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    /**
     * 功能：保存采集结果到数据库并缓存到 Redis。
     *
     * @param result 采集结果
     */
    public void saveAndCache(HotSearchResult result) {
        if (result == null || result.items() == null || result.items().isEmpty()) {
            log.warn("采集结果为空，跳过保存");
            return;
        }

        // 删除同平台当天的旧批次，只保留最新一批
        LocalDateTime dayStart = result.collectedAt().toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        remove(new LambdaQueryWrapper<HotSearchRecord>()
                .eq(HotSearchRecord::getPlatform, result.platform())
                .ge(HotSearchRecord::getCollectedAt, dayStart)
                .lt(HotSearchRecord::getCollectedAt, dayEnd));

        List<HotSearchRecord> records = result.items().stream()
                .map(item -> toRecord(item, result.platform(), result.collectedAt()))
                .toList();

        saveBatch(records);
        log.info("平台 [{}] 热搜数据已入库，共 {} 条（已清理当天旧数据）", result.platform(), records.size());

        String redisKey = REDIS_KEY_PREFIX + result.platform() + REDIS_KEY_SUFFIX;
        RedisUtils.setCacheObject(redisKey, result, CACHE_TTL);
        log.info("平台 [{}] 热搜数据已缓存至 Redis，TTL {} 小时", result.platform(), CACHE_TTL.toHours());
    }

    /**
     * 功能：获取指定平台的最新一批热搜数据（优先读 Redis 缓存，缓存未命中或反序列化失败则回源数据库）。
     * <p>
     * 注意：如果 Redis 中存在旧格式的缓存数据（导致反序列化失败），
     * 会自动清理旧缓存并从数据库重新加载。
     *
     * @param platform 平台枚举
     * @return 热搜结果（无数据返回 null）
     */
    public HotSearchResult getLatest(Platform platform) {
        String platformCode = platform.getCode();
        String redisKey = REDIS_KEY_PREFIX + platformCode + REDIS_KEY_SUFFIX;
        try {
            HotSearchResult cached = RedisUtils.getCacheObject(redisKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            // 旧格式缓存数据导致反序列化失败，清理旧缓存并从数据库重新加载
            log.warn("Redis 缓存反序列化失败，清理旧缓存，键名：{}", redisKey);
            RedisUtils.deleteObject(redisKey);
        }

        // 缓存未命中或已清理，从数据库查询最新一批
        LocalDateTime latestTime = getLatestCollectedAt(platformCode);
        if (latestTime == null) {
            return null;
        }

        List<HotSearchRecord> records = list(new LambdaQueryWrapper<HotSearchRecord>()
                .eq(HotSearchRecord::getPlatform, platformCode)
                .eq(HotSearchRecord::getCollectedAt, latestTime)
                .orderByAsc(HotSearchRecord::getRankNum));

        if (records.isEmpty()) {
            return null;
        }

        List<HotSearchItem> items = records.stream()
                .map(this::toItem)
                .toList();

        HotSearchResult result = new HotSearchResult(platformCode, items, latestTime);

        // 回填缓存
        RedisUtils.setCacheObject(redisKey, result, CACHE_TTL);
        return result;
    }

    /**
     * 功能：查询指定平台指定日期的热搜记录。
     *
     * @param platform 平台枚举
     * @param date     查询日期
     * @return 热搜结果（无数据返回 null）
     */
    public HotSearchResult getByDate(Platform platform, LocalDate date) {
        String platformCode = platform.getCode();
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<HotSearchRecord> records = list(new LambdaQueryWrapper<HotSearchRecord>()
                .eq(HotSearchRecord::getPlatform, platformCode)
                .ge(HotSearchRecord::getCollectedAt, dayStart)
                .lt(HotSearchRecord::getCollectedAt, dayEnd)
                .orderByDesc(HotSearchRecord::getCollectedAt)
                .orderByAsc(HotSearchRecord::getRankNum));

        if (records.isEmpty()) {
            return null;
        }

        List<HotSearchItem> items = records.stream()
                .map(this::toItem)
                .toList();

        return new HotSearchResult(platformCode, items, records.get(0).getCollectedAt());
    }

    /**
     * 功能：查询指定平台最新一次采集的时间。
     *
     * @param platform 平台标识
     * @return 最新采集时间（无数据返回 null）
     */
    private LocalDateTime getLatestCollectedAt(String platform) {
        HotSearchRecord record = getOne(new LambdaQueryWrapper<HotSearchRecord>()
                .eq(HotSearchRecord::getPlatform, platform)
                .orderByDesc(HotSearchRecord::getCollectedAt)
                .select(HotSearchRecord::getCollectedAt)
                .last("LIMIT 1"));
        return record != null ? record.getCollectedAt() : null;
    }

    private HotSearchRecord toRecord(HotSearchItem item, String platform, LocalDateTime collectedAt) {
        HotSearchRecord record = new HotSearchRecord();
        record.setPlatform(platform);
        record.setRankNum(item.rank());
        record.setTitle(item.title());
        record.setHotScore(item.hotScore());
        record.setUrl(item.url());
        record.setHotTag(item.hotTag());
        record.setCollectedAt(collectedAt);
        return record;
    }

    private HotSearchItem toItem(HotSearchRecord record) {
        return new HotSearchItem(
                record.getRankNum(),
                record.getTitle(),
                record.getHotScore(),
                record.getUrl(),
                record.getHotTag()
        );
    }
}
