package ai.skills.api.hotsearch.collector;

import ai.skills.api.hotsearch.HotSearchCollector;
import ai.skills.api.hotsearch.HotSearchItem;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.collector.model.WeiboBoardResponse;
import ai.skills.api.hotsearch.collector.model.WeiboBoardResponse.HotItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：微博热搜采集器，自动生成访客 Cookie 后通过微博热搜榜 API 获取实时热搜数据。
 * <p>
 * Bean 名称 {@code "weibo"} 与 YAML 中 {@code skills-api.scheduler.platforms.weibo} 对应，
 * 调度系统自动按名称匹配注册。
 * <p>
 * Cookie 获取流程（无需手动配置）：
 * <ol>
 *     <li>POST {@code passport.weibo.com/visitor/genvisitor} 获取访客票据 tid</li>
 *     <li>GET {@code passport.weibo.com/visitor/visitor?a=incarnate&t=tid} 生成 SUB/SUBP Cookie</li>
 *     <li>HttpClient CookieManager 自动管理 Cookie，后续 API 请求自动携带</li>
 * </ol>
 * 作者：Devil
 */
@Slf4j
@Component("weibo")
public class WeiboHotSearchCollector implements HotSearchCollector {

    private static final String PLATFORM = "weibo";
    private static final String API_URL = "https://weibo.com/ajax/side/searchBand?type=hot";
    private static final String SEARCH_URL_TEMPLATE = "https://s.weibo.com/weibo?q=%s";
    private static final String GEN_VISITOR_URL = "https://passport.weibo.com/visitor/genvisitor";
    private static final String INCARNATE_URL = "https://passport.weibo.com/visitor/visitor";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    /** Cookie 有效期：1 小时后自动刷新 */
    private static final long COOKIE_TTL_MS = Duration.ofHours(1).toMillis();

    private final HttpClient httpClient;
    private final CookieManager cookieManager;
    private final ObjectMapper objectMapper;
    private volatile long lastCookieTime;

    public WeiboHotSearchCollector() {
        this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String platform() {
        return PLATFORM;
    }

    @Override
    public HotSearchResult collect() {
        log.info("微博热搜采集开始");

        ensureCookie();
        WeiboBoardResponse response = fetchApi();
        List<HotSearchItem> items = mapToItems(response);

        // API 返回未授权时，刷新 Cookie 后重试一次
        if (items.isEmpty() && (response == null || response.ok() != 1)) {
            log.info("微博 API 返回异常，刷新 Cookie 后重试");
            refreshCookie();
            response = fetchApi();
            items = mapToItems(response);
        }

        log.info("微博热搜采集结束，共 {} 条", items.size());
        return new HotSearchResult(PLATFORM, items, LocalDateTime.now());
    }

    /**
     * 功能：确保 Cookie 在有效期内，过期则自动刷新。
     */
    private void ensureCookie() {
        if (System.currentTimeMillis() - lastCookieTime < COOKIE_TTL_MS) {
            return;
        }
        refreshCookie();
    }

    /**
     * 功能：通过微博 Passport 访客接口自动生成 Cookie。
     * <p>
     * 流程：genvisitor 获取 tid → incarnate 换取 SUB/SUBP Cookie（由 CookieManager 自动存储）。
     */
    private void refreshCookie() {
        try {
            // 清理旧 Cookie
            cookieManager.getCookieStore().removeAll();

            // Step 1: 获取访客票据 tid
            String fp = "{\"os\":\"1\",\"browser\":\"Chrome131,0,0,0\",\"fonts\":\"undefined\","
                    + "\"screenInfo\":\"1920*1080*24\",\"plugins\":\"\"}";
            String body = "cb=gen_callback&fp=" + URLEncoder.encode(fp, StandardCharsets.UTF_8);

            HttpRequest genRequest = HttpRequest.newBuilder()
                    .uri(URI.create(GEN_VISITOR_URL))
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> genResponse = httpClient.send(genRequest, HttpResponse.BodyHandlers.ofString());
            String tid = parseJsonpField(genResponse.body(), "tid");

            if (tid == null) {
                log.warn("获取微博访客 tid 失败，响应：{}", genResponse.body());
                return;
            }

            // Step 2: 用 tid 换取 SUB/SUBP Cookie
            String incarnateUrl = INCARNATE_URL
                    + "?a=incarnate&t=" + URLEncoder.encode(tid, StandardCharsets.UTF_8)
                    + "&w=2&c=095&gc=&cb=cross_domain&from=&_rand=" + Math.random();

            HttpRequest incarnateRequest = HttpRequest.newBuilder()
                    .uri(URI.create(incarnateUrl))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            httpClient.send(incarnateRequest, HttpResponse.BodyHandlers.ofString());
            // Cookie 由 CookieManager 自动存储，后续请求自动携带

            lastCookieTime = System.currentTimeMillis();
            log.info("微博访客 Cookie 已自动生成");
        } catch (Exception e) {
            log.error("生成微博访客 Cookie 失败", e);
        }
    }

    /**
     * 功能：调用微博热搜 API，返回反序列化后的响应对象。
     */
    private WeiboBoardResponse fetchApi() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", "https://weibo.com/hot/search")
                    .header("Accept", "application/json")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), WeiboBoardResponse.class);
        } catch (Exception e) {
            log.error("调用微博热搜 API 失败", e);
            return null;
        }
    }

    /**
     * 功能：从 JSONP 响应中提取指定字段值。
     * <p>
     * 响应格式：{@code window.xxx && xxx({"retcode":20000000,"data":{"tid":"xxx",...}})}
     *
     * @param jsonp     JSONP 响应字符串
     * @param fieldName data 层级下的字段名
     * @return 字段值，解析失败返回 null
     */
    private String parseJsonpField(String jsonp, String fieldName) {
        try {
            int start = jsonp.indexOf('(');
            int end = jsonp.lastIndexOf(')');
            if (start < 0 || end <= start) {
                return null;
            }
            String json = jsonp.substring(start + 1, end);
            JsonNode root = objectMapper.readTree(json);
            if (root.path("retcode").asInt() != 20000000) {
                return null;
            }
            JsonNode value = root.path("data").path(fieldName);
            return value.isMissingNode() ? null : value.asText();
        } catch (Exception e) {
            log.error("解析微博 JSONP 响应失败", e);
            return null;
        }
    }

    /**
     * 功能：将 API 响应映射为统一的 {@link HotSearchItem} 列表。
     */
    private List<HotSearchItem> mapToItems(WeiboBoardResponse response) {
        if (response == null || response.ok() != 1 || response.data() == null) {
            log.warn("微博热搜 API 响应异常或未授权（ok={}）", response != null ? response.ok() : "null");
            return Collections.emptyList();
        }

        List<HotItem> hotItems = response.data().realtime();
        if (hotItems == null || hotItems.isEmpty()) {
            log.warn("微博热搜 API 响应中无 realtime 数据");
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
     */
    private HotSearchItem toHotSearchItem(HotItem item, int defaultRank) {
        int rank = item.realpos() != null ? item.realpos() :
                (item.rank() != null ? item.rank() + 1 : defaultRank);
        String title = item.word() != null ? item.word() : "";
        long hotScore = item.num() != null ? item.num() : 0;
        String url = buildSearchUrl(title);
        String hotTag = item.iconDesc() != null && !item.iconDesc().isBlank() ?
                item.iconDesc() :
                (item.labelName() != null ? item.labelName() : "");
        return new HotSearchItem(rank, title, hotScore, url, hotTag);
    }

    private String buildSearchUrl(String word) {
        if (word == null || word.isBlank()) {
            return "";
        }
        return String.format(SEARCH_URL_TEMPLATE, URLEncoder.encode(word, StandardCharsets.UTF_8));
    }
}
