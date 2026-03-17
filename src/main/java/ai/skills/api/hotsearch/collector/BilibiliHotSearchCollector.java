package ai.skills.api.hotsearch.collector;

import ai.skills.api.hotsearch.HotSearchCollector;
import ai.skills.api.hotsearch.HotSearchItem;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.collector.model.BilibiliPopularResponse;
import ai.skills.api.hotsearch.collector.model.BilibiliPopularResponse.VideoItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 创建时间：2026/03/17
 * 功能：Bilibili 热门视频采集器，通过 Bilibili API 获取热门视频数据。
 * <p>
 * Bean 名称 {@code "bilibili"} 与 YAML 中 {@code skills-api.scheduler.platforms.bilibili} 对应，
 * 调度系统自动按名称匹配注册。
 * <p>
 * 综合热度计算公式：hotScore = view + like * 10 + coin * 20 + share * 50
 * 作者：Devil
 */
@Slf4j
@Component("bilibili")
public class BilibiliHotSearchCollector implements HotSearchCollector {

    private static final String PLATFORM = "bilibili";
    private static final String API_URL = "https://api.bilibili.com/x/web-interface/popular?ps=50";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    /** 热度权重配置 */
    private static final long LIKE_WEIGHT = 10;
    private static final long COIN_WEIGHT = 20;
    private static final long SHARE_WEIGHT = 50;

    private final RestTemplate restTemplate;

    public BilibiliHotSearchCollector() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String platform() {
        return PLATFORM;
    }

    @Override
    public HotSearchResult collect() {
        log.info("Bilibili 热门视频采集开始，API：{}", API_URL);

        BilibiliPopularResponse response = fetchApi();
        List<HotSearchItem> items = mapToItems(response);

        log.info("Bilibili 热门视频采集结束，共 {} 条", items.size());
        return new HotSearchResult(PLATFORM, items, LocalDateTime.now());
    }

    /**
     * 功能：调用 Bilibili 热门视频 API，返回反序列化后的响应对象。
     *
     * @return API 响应实体
     */
    private BilibiliPopularResponse fetchApi() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.REFERER, "https://www.bilibili.com");

        ResponseEntity<BilibiliPopularResponse> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, new HttpEntity<>(headers), BilibiliPopularResponse.class
        );
        return response.getBody();
    }

    /**
     * 功能：将 API 响应映射为统一的 {@link HotSearchItem} 列表，按综合热度降序排列。
     *
     * @param response API 响应
     * @return 热搜条目列表
     */
    private List<HotSearchItem> mapToItems(BilibiliPopularResponse response) {
        if (response == null || !response.success() || response.data() == null) {
            log.warn("Bilibili API 响应异常或为空");
            return Collections.emptyList();
        }

        List<VideoItem> videoItems = response.data().list();
        if (videoItems == null || videoItems.isEmpty()) {
            log.warn("Bilibili API 响应中无视频数据");
            return Collections.emptyList();
        }

        // 计算综合热度并排序
        List<HotSearchItem> items = new ArrayList<>(videoItems.size());
        for (VideoItem video : videoItems) {
            items.add(toHotSearchItem(video));
        }

        // 按热度降序排列
        items.sort(Comparator.comparingLong(HotSearchItem::hotScore).reversed());

        // 重新设置排名
        List<HotSearchItem> rankedItems = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            HotSearchItem item = items.get(i);
            rankedItems.add(new HotSearchItem(
                    i + 1,
                    item.title(),
                    item.hotScore(),
                    item.url(),
                    item.hotTag()
            ));
        }

        return rankedItems;
    }

    /**
     * 功能：将单个视频条目转换为 {@link HotSearchItem}。
     *
     * @param video 视频条目
     * @return 统一热搜条目
     */
    private HotSearchItem toHotSearchItem(VideoItem video) {
        String title = video.title() != null ? video.title() : "";
        long hotScore = calculateHotScore(video.stat());
        String url = video.shortLinkV2() != null ? video.shortLinkV2() : "";
        return new HotSearchItem(0, title, hotScore, url, "热");
    }

    /**
     * 功能：计算综合热度值。
     * <p>
     * 公式：hotScore = view + like * 10 + coin * 20 + share * 50
     *
     * @param stat 视频统计数据
     * @return 综合热度值
     */
    private long calculateHotScore(BilibiliPopularResponse.Stat stat) {
        if (stat == null) {
            return 0;
        }
        return stat.view()
                + stat.like() * LIKE_WEIGHT
                + stat.coin() * COIN_WEIGHT
                + stat.share() * SHARE_WEIGHT;
    }
}
