package ai.skills.api.stockindex.collector;

import ai.skills.api.common.cache.CacheService;
import ai.skills.api.stockindex.config.StockIndexProperties;
import ai.skills.api.stockindex.model.IndexQuote;
import ai.skills.api.stockindex.model.StockIndexResult;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2026/03/18
 * 功能：股票指数数据采集器，从养基宝 API（app-api.yangjibao.com）采集全球主要指数实时行情数据。
 * <p>
 * 采集逻辑：
 * <ol>
 *     <li>调用养基宝指数接口，一次请求获取全部指数数据</li>
 *     <li>解析 JSON 响应，提取关键字段</li>
 *     <li>写入缓存，默认 TTL 5 分钟</li>
 * </ol>
 * 作者：Devil
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockIndexCollector {

    /**
     * 养基宝指数行情 API 地址
     */
    private static final String API_URL = "https://app-api.yangjibao.com/market/v1/quote/index-data";

    /**
     * 请求超时时间（毫秒）
     */
    private static final int TIMEOUT_MS = 10000;

    /**
     * User-Agent 请求头
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    /**
     * 缓存键
     */
    private static final String CACHE_KEY = "stockindex:all";

    private final StockIndexProperties properties;
    private final CacheService cacheService;

    /**
     * 功能：采集指定分组代码的指数行情数据。
     * <p>
     * 从全量数据中筛选出目标代码的指数。
     *
     * @param groupCode 分组代码（如 a-share, hk, us）
     * @param groupName 分组名称（如 A股指数）
     * @param targetCodes 目标指数代码列表
     * @return 分组行情结果
     */
    public ai.skills.api.stockindex.model.StockIndexResult collect(String groupCode, String groupName, List<String> targetCodes) {
        List<IndexQuote> allQuotes = collectAll();

        // 筛选出目标代码的指数
        List<IndexQuote> filteredQuotes = allQuotes.stream()
                .filter(quote -> targetCodes.contains(quote.code()))
                .toList();

        return new StockIndexResult(groupCode, groupName, filteredQuotes, LocalDateTime.now());
    }

    /**
     * 功能：采集全部指数行情数据（优先读取缓存）。
     * <p>
     * 若缓存为空，则自动触发一次采集。
     *
     * @return 全部指数行情列表
     */
    public List<IndexQuote> collectAll() {
        // 1. 尝试从缓存获取
        try {
            List<IndexQuote> cached = cacheService.get(CACHE_KEY);
            if (cached != null) {
                log.debug("命中股票指数缓存");
                return cached;
            }
        } catch (Exception e) {
            log.warn("缓存反序列化失败，清理旧缓存");
            cacheService.delete(CACHE_KEY);
        }

        // 2. 缓存未命中，执行采集
        try {
            List<IndexQuote> quotes = fetchQuotes();
            if (!quotes.isEmpty()) {
                cacheService.set(CACHE_KEY, quotes, properties.getCacheTtl());
                log.debug("股票指数数据已缓存，共 {} 条，TTL={}", quotes.size(), properties.getCacheTtl());
            }
            return quotes;
        } catch (Exception e) {
            log.error("采集股票指数数据失败", e);
            return List.of();
        }
    }

    /**
     * 功能：调用养基宝 API 获取全部指数行情数据并解析。
     *
     * @return 指数行情列表
     */
    private List<IndexQuote> fetchQuotes() {
        log.info("开始采集股票指数数据，API：{}", API_URL);

        HttpResponse response = HttpRequest.get(API_URL)
                .header("User-Agent", USER_AGENT)
                .timeout(TIMEOUT_MS)
                .execute();

        if (!response.isOk() || response.body() == null) {
            log.warn("养基宝 API 请求失败，状态码：{}", response.getStatus());
            return List.of();
        }

        return parseResponse(response.body());
    }

    /**
     * 功能：解析养基宝 API 返回的 JSON 数据。
     * <p>
     * 返回格式：
     * <pre>
     * {"code":200, "data": [
     *   {"code":"1.000001", "name":"上证指数", "v":4034.01, "dir":-0.39, "div":-15.9,
     *    "m":"623439225042.9", "uc":"975", "dc":"1313", "nc":"56", "date":"2026-03-18 13:22:31"},
     *   ...
     * ]}
     * </pre>
     * 字段说明：
     * - code: 内部代码, show_code: 展示代码, name: 指数名称
     * - v: 当前价, dir: 涨跌幅(%), div: 涨跌额
     * - m: 成交额, uc: 上涨家数, dc: 下跌家数, nc: 平盘家数
     * - date: 更新时间
     *
     * @param body API 响应体
     * @return 指数行情列表
     */
    private List<IndexQuote> parseResponse(String body) {
        List<IndexQuote> quotes = new ArrayList<>();

        try {
            JSONObject json = JSONUtil.parseObj(body);
            if (!json.getInt("code", 0).equals(200)) {
                log.warn("养基宝 API 返回错误：{}", json.getStr("message"));
                return List.of();
            }

            JSONArray data = json.getJSONArray("data");
            if (data == null || data.isEmpty()) {
                return List.of();
            }

            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);

                String code = item.getStr("code", "");
                String showCode = item.getStr("show_code", "");
                String name = item.getStr("name", "");
                String date = item.getStr("date", "");

                // 解析日期时间
                String dateStr = "";
                String timeStr = "";
                if (date.contains(" ")) {
                    String[] parts = date.split(" ", 2);
                    dateStr = parts[0];
                    timeStr = parts.length > 1 ? parts[1] : "";
                } else {
                    dateStr = date;
                }

                IndexQuote quote = new IndexQuote(
                        showCode,
                        name,
                        item.getStr("v", ""),
                        item.getStr("div", ""),
                        item.getStr("dir", ""),
                        item.getStr("m", ""),
                        item.getStr("uc", "0"),
                        item.getStr("dc", "0"),
                        item.getStr("nc", "0"),
                        dateStr,
                        timeStr
                );

                quotes.add(quote);
            }

            log.info("股票指数数据解析完成，共 {} 条有效数据", quotes.size());
        } catch (Exception e) {
            log.error("解析养基宝 API 响应失败", e);
        }

        return quotes;
    }
}
