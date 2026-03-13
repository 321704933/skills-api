package ai.skills.api.hotsearch.collector;

import ai.skills.api.hotsearch.HotSearchCollector;
import ai.skills.api.hotsearch.HotSearchItem;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.collector.model.BaiduBoardResponse;
import ai.skills.api.hotsearch.collector.model.BaiduBoardResponse.HotItem;
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
import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：百度热搜采集器，通过百度热搜榜 API 获取实时热搜数据。
 * <p>
 * Bean 名称 {@code "baidu"} 与 YAML 中 {@code skills-api.scheduler.platforms.baidu} 对应，
 * 调度系统自动按名称匹配注册。
 * <p>
 * API 响应通过 {@link BaiduBoardResponse} 实体类进行 JSON 反序列化，
 * 结构路径：{@code data.cards[0].content[]} → {@link HotItem}。
 * 作者：Devil
 */
@Slf4j
@Component("baidu")
public class BaiduHotSearchCollector implements HotSearchCollector {

    private static final String PLATFORM = "baidu";
    private static final String API_URL = "https://top.baidu.com/api/board?platform=pc&tab=realtime";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    /** hotTag 编码 → 中文标签 */
    private static final java.util.Map<String, String> HOT_TAG_MAP = java.util.Map.of(
            "1", "新",
            "2", "沸",
            "3", "热"
    );

    private final RestTemplate restTemplate;

    public BaiduHotSearchCollector() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String platform() {
        return PLATFORM;
    }

    @Override
    public HotSearchResult collect() {
        log.info("百度热搜采集开始，API：{}", API_URL);

        BaiduBoardResponse response = fetchApi();
        List<HotSearchItem> items = mapToItems(response);

        log.info("百度热搜采集结束，共 {} 条", items.size());
        return new HotSearchResult(PLATFORM, items, LocalDateTime.now());
    }

    /**
     * 功能：调用百度热搜 API，返回反序列化后的响应对象。
     *
     * @return API 响应实体
     */
    private BaiduBoardResponse fetchApi() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<BaiduBoardResponse> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, new HttpEntity<>(headers), BaiduBoardResponse.class
        );
        return response.getBody();
    }

    /**
     * 功能：将 API 响应映射为统一的 {@link HotSearchItem} 列表。
     *
     * @param response API 响应
     * @return 热搜条目列表
     */
    private List<HotSearchItem> mapToItems(BaiduBoardResponse response) {
        if (response == null || !response.success() || response.data() == null) {
            log.warn("百度热搜 API 响应异常或为空");
            return Collections.emptyList();
        }

        List<BaiduBoardResponse.Card> cards = response.data().cards();
        if (cards == null || cards.isEmpty()) {
            log.warn("百度热搜 API 响应中无 cards 数据");
            return Collections.emptyList();
        }

        List<HotItem> hotItems = cards.get(0).content();
        if (hotItems == null || hotItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<HotSearchItem> items = new ArrayList<>(hotItems.size());
        for (HotItem hotItem : hotItems) {
            items.add(toHotSearchItem(hotItem, items.size() + 1));
        }
        return items;
    }

    /**
     * 功能：将单个 API 条目转换为 {@link HotSearchItem}。
     *
     * @param item        API 条目
     * @param defaultRank 默认排名（当条目无 index 字段时使用）
     * @return 统一热搜条目
     */
    private HotSearchItem toHotSearchItem(HotItem item, int defaultRank) {
        int rank = item.index() != null ? item.index() + 1 : defaultRank;
        String title = item.word() != null ? item.word() : "";
        long hotScore = parseHotScore(item.hotScore());
        String url = item.appUrl() != null ? item.appUrl() : (item.url() != null ? item.url() : "");
        String hotTag = HOT_TAG_MAP.getOrDefault(item.hotTag(), "");
        return new HotSearchItem(rank, title, hotScore, url, hotTag);
    }

    private long parseHotScore(String hotScore) {
        if (hotScore == null || hotScore.isBlank()) {
            return 0;
        }
        try {
            return Long.parseLong(hotScore);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
