package ai.skills.api.hotsearch.collector;

import ai.skills.api.hotsearch.HotSearchCollector;
import ai.skills.api.hotsearch.HotSearchItem;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.collector.model.ToutiaoBoardResponse;
import ai.skills.api.hotsearch.collector.model.ToutiaoBoardResponse.HotItem;
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
 * 功能：今日头条热榜采集器，通过头条热榜 API 获取实时热榜数据。
 * <p>
 * Bean 名称 {@code "toutiao"} 与 YAML 中 {@code skills-api.scheduler.platforms.toutiao} 对应，
 * 调度系统自动按名称匹配注册。
 * <p>
 * API 响应通过 {@link ToutiaoBoardResponse} 实体类进行 JSON 反序列化，
 * 结构路径：{@code data[]} → {@link HotItem}。
 * 作者：Devil
 */
@Slf4j
@Component("toutiao")
public class ToutiaoHotSearchCollector implements HotSearchCollector {

    private static final String PLATFORM = "toutiao";
    private static final String API_URL = "https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    private final RestTemplate restTemplate;

    public ToutiaoHotSearchCollector() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String platform() {
        return PLATFORM;
    }

    @Override
    public HotSearchResult collect() {
        log.info("今日头条热榜采集开始，API：{}", API_URL);

        ToutiaoBoardResponse response = fetchApi();
        List<HotSearchItem> items = mapToItems(response);

        log.info("今日头条热榜采集结束，共 {} 条", items.size());
        return new HotSearchResult(PLATFORM, items, LocalDateTime.now());
    }

    /**
     * 功能：调用今日头条热榜 API，返回反序列化后的响应对象。
     *
     * @return API 响应实体
     */
    private ToutiaoBoardResponse fetchApi() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.REFERER, "https://www.toutiao.com/");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<ToutiaoBoardResponse> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, new HttpEntity<>(headers), ToutiaoBoardResponse.class
        );
        return response.getBody();
    }

    /**
     * 功能：将 API 响应映射为统一的 {@link HotSearchItem} 列表。
     *
     * @param response API 响应
     * @return 热搜条目列表
     */
    private List<HotSearchItem> mapToItems(ToutiaoBoardResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            log.warn("今日头条热榜 API 响应为空");
            return Collections.emptyList();
        }

        List<HotItem> hotItems = response.data();
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
     * @param defaultRank 默认排名（数组下标 + 1）
     * @return 统一热搜条目
     */
    private HotSearchItem toHotSearchItem(HotItem item, int defaultRank) {
        String title = item.title() != null ? item.title() : "";
        long hotScore = parseHotValue(item.hotValue());
        String url = item.url() != null ? item.url() : "";
        String hotTag = item.label() != null ? item.label() : "";
        return new HotSearchItem(defaultRank, title, hotScore, url, hotTag);
    }

    private long parseHotValue(String hotValue) {
        if (hotValue == null || hotValue.isBlank()) {
            return 0;
        }
        try {
            return Long.parseLong(hotValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
