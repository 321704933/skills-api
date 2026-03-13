package ai.skills.api.hotsearch.collector;

import ai.skills.api.hotsearch.HotSearchCollector;
import ai.skills.api.hotsearch.HotSearchItem;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.collector.model.DouyinBoardResponse;
import ai.skills.api.hotsearch.collector.model.DouyinBoardResponse.HotItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 创建时间：2026/03/13
 * 功能：抖音热搜采集器，通过抖音热搜榜 API 获取实时热搜数据。
 * <p>
 * Bean 名称 {@code "douyin"} 与 YAML 中 {@code skills-api.scheduler.platforms.douyin} 对应，
 * 调度系统自动按名称匹配注册。
 * <p>
 * API 响应通过 {@link DouyinBoardResponse} 实体类进行 JSON 反序列化，
 * 主榜单路径：{@code data.word_list[]} → {@link HotItem}。
 * 作者：Devil
 */
@Slf4j
@Component("douyin")
public class DouyinHotSearchCollector implements HotSearchCollector {

    private static final String PLATFORM = "douyin";
    private static final String API_URL = "https://www.douyin.com/aweme/v1/web/hot/search/list/";
    private static final String SEARCH_URL_TEMPLATE = "https://www.douyin.com/search/%s?type=general";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    /** label 编码 → 中文标签 */
    private static final Map<Integer, String> LABEL_MAP = Map.of(
            1, "新",
            3, "热",
            5, "独家",
            8, "推荐"
    );

    private final RestTemplate restTemplate;

    public DouyinHotSearchCollector() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String platform() {
        return PLATFORM;
    }

    @Override
    public HotSearchResult collect() {
        log.info("抖音热搜采集开始，API：{}", API_URL);

        DouyinBoardResponse response = fetchApi();
        List<HotSearchItem> items = mapToItems(response);

        log.info("抖音热搜采集结束，共 {} 条", items.size());
        return new HotSearchResult(PLATFORM, items, LocalDateTime.now());
    }

    /**
     * 功能：调用抖音热搜 API，返回反序列化后的响应对象。
     *
     * @return API 响应实体
     */
    private DouyinBoardResponse fetchApi() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.REFERER, "https://www.douyin.com/");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<DouyinBoardResponse> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, new HttpEntity<>(headers), DouyinBoardResponse.class
        );
        return response.getBody();
    }

    /**
     * 功能：将 API 响应映射为统一的 {@link HotSearchItem} 列表。
     *
     * @param response API 响应
     * @return 热搜条目列表
     */
    private List<HotSearchItem> mapToItems(DouyinBoardResponse response) {
        if (response == null || response.statusCode() != 0 || response.data() == null) {
            log.warn("抖音热搜 API 响应异常或为空");
            return Collections.emptyList();
        }

        List<HotItem> hotItems = response.data().wordList();
        if (hotItems == null || hotItems.isEmpty()) {
            log.warn("抖音热搜 API 响应中无 word_list 数据");
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
     * @param defaultRank 默认排名
     * @return 统一热搜条目
     */
    private HotSearchItem toHotSearchItem(HotItem item, int defaultRank) {
        int rank = item.position() != null ? item.position() : defaultRank;
        String title = item.word() != null ? item.word() : "";
        long hotScore = item.hotValue() != null ? item.hotValue() : 0;
        String url = buildSearchUrl(title);
        String hotTag = LABEL_MAP.getOrDefault(item.label(), "");
        return new HotSearchItem(rank, title, hotScore, url, hotTag);
    }

    private String buildSearchUrl(String word) {
        if (word == null || word.isBlank()) {
            return "";
        }
        return String.format(SEARCH_URL_TEMPLATE, URLEncoder.encode(word, StandardCharsets.UTF_8));
    }
}
