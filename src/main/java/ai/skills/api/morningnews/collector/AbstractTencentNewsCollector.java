package ai.skills.api.morningnews.collector;

import ai.skills.api.morningnews.MorningNewsCollector;
import ai.skills.api.morningnews.MorningNewsItem;
import ai.skills.api.morningnews.MorningNewsResult;
import ai.skills.api.morningnews.NewsCategory;
import ai.skills.api.morningnews.collector.model.TencentNewsResponse;
import ai.skills.api.morningnews.collector.model.TencentNewsResponse.NewsItem;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 创建时间：2026/03/17
 * 功能：腾讯新闻早报采集器抽象基类，提供通用的采集逻辑。
 * <p>
 * 子类只需实现 {@link #getCategory()} 方法返回对应的分类枚举。
 * 作者：Devil
 */
@Slf4j
public abstract class AbstractTencentNewsCollector implements MorningNewsCollector {

    // 腾讯新闻早报 API
    protected static final String API_URL = "https://i.news.qq.com/web_backend/v2/getTagInfo";

    /**
     * User-Agent 请求头
     */
    protected static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    /**
     * 功能：获取采集器对应的新闻分类。
     *
     * @return 新闻分类枚举
     */
    protected abstract NewsCategory getCategory();

    @Override
    public String category() {
        return getCategory().getCode();
    }

    @Override
    public MorningNewsResult collect() {
        NewsCategory category = getCategory();
        log.info("{} 采集开始，Tag ID：{}", category.getName(), category.getTagId());

        TencentNewsResponse response = fetchApi(category);
        List<MorningNewsItem> items = mapToItems(response);

        log.info("{} 采集结束，共 {} 条", category.getName(), items.size());
        return new MorningNewsResult(
                category.getCode(),
                category.getName(),
                items,
                LocalDateTime.now()
        );
    }

    /**
     * 功能：调用腾讯新闻 API，返回反序列化后的响应对象。
     *
     * @param category 新闻分类
     * @return API 响应实体
     */
    protected TencentNewsResponse fetchApi(NewsCategory category) {
        try {
            String url = API_URL + "?tagId=" + category.getTagId() + "&num=30";

            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", USER_AGENT);
            headers.put("Accept", "application/json, text/plain, */*");
            headers.put("Referer", "https://news.qq.com/");
            headers.put("Origin", "https://news.qq.com");
            // 添加基础 Cookie（腾讯新闻常见 Cookie）
            headers.put("Cookie", "pgv_pvid=" + System.currentTimeMillis() + ";");

            HttpResponse httpResponse = HttpRequest.get(url)
                    .headerMap(headers, false)
                    .timeout(10000)
                    .execute();

            if (httpResponse.isOk()) {
                String responseBody = httpResponse.body();
                if (StrUtil.isNotBlank(responseBody)) {
                    return parseApiResponse(responseBody);
                }
            }
        } catch (Exception e) {
            log.warn("API 方式采集失败，尝试 HTML 解析方式", e);
        }

        // 如果 API 方式失败，尝试 HTML 解析方式
        return fetchFromHtml(category);
    }

    /**
     * 功能：解析 API 响应 JSON。
     * <p>
     * 腾讯新闻早报 API 返回的 JSON 结构如下：
     * <pre>
     * {
     *   "ret": 0,
     *   "msg": "ok",
     *   "data": {
     *     "tabs": [{
     *       "articleList": [{...}, {...}]
     *     }]
     *   }
     * }
     * </pre>
     *
     * @param jsonStr JSON 字符串
     * @return 响应对象
     */
    protected TencentNewsResponse parseApiResponse(String jsonStr) {
        try {
            JSONObject json = JSONUtil.parseObj(jsonStr);
            int ret = json.getInt("ret", -1);
            String msg = json.getStr("msg", "");

            JSONObject dataObj = json.getJSONObject("data");
            if (dataObj == null) {
                return new TencentNewsResponse(ret, msg, null);
            }

            // 新版 API 结构：data.tabs[].articleList[]
            JSONArray tabsArray = dataObj.getJSONArray("tabs");
            if (tabsArray != null && !tabsArray.isEmpty()) {
                List<NewsItem> items = new ArrayList<>();
                for (int i = 0; i < tabsArray.size() && items.size() < 30; i++) {
                    JSONObject tabObj = tabsArray.getJSONObject(i);
                    if (tabObj == null) {
                        continue;
                    }
                    JSONArray articleList = tabObj.getJSONArray("articleList");
                    if (articleList == null || articleList.isEmpty()) {
                        continue;
                    }
                    for (int j = 0; j < articleList.size() && items.size() < 30; j++) {
                        JSONObject itemObj = articleList.getJSONObject(j);
                        if (itemObj == null) {
                            continue;
                        }
                        NewsItem item = parseArticleItem(itemObj);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                }
                return new TencentNewsResponse(ret, msg, new TencentNewsResponse.Data(items));
            }

            // 兼容旧版 API 结构：data.info[]
            JSONArray infoArray = dataObj.getJSONArray("info");
            if (infoArray == null || infoArray.isEmpty()) {
                return new TencentNewsResponse(ret, msg, new TencentNewsResponse.Data(Collections.emptyList()));
            }

            List<NewsItem> items = new ArrayList<>();
            for (int i = 0; i < infoArray.size(); i++) {
                JSONObject itemObj = infoArray.getJSONObject(i);
                if (itemObj == null) {
                    continue;
                }

                NewsItem item = new NewsItem(
                        itemObj.getStr("id"),
                        itemObj.getStr("title"),
                        itemObj.getStr("intro"),
                        itemObj.getStr("source"),
                        itemObj.getStr("time"),
                        itemObj.getStr("url"),
                        itemObj.getStr("img"),
                        itemObj.getStr("channel"),
                        itemObj.getStr("tag")
                );
                items.add(item);
            }

            return new TencentNewsResponse(ret, msg, new TencentNewsResponse.Data(items));
        } catch (Exception e) {
            log.warn("解析 API 响应失败", e);
            return null;
        }
    }

    /**
     * 功能：解析新版 API 中的文章条目。
     * <p>
     * 新版 API 的文章字段结构：
     * <ul>
     *   <li>id - 文章 ID</li>
     *   <li>title - 标题</li>
     *   <li>desc - 摘要</li>
     *   <li>media_info.chl_name - 来源媒体名称</li>
     *   <li>publish_time - 发布时间</li>
     *   <li>link_info.url - 文章链接</li>
     *   <li>pic_info.big_img[0] - 封面图片</li>
     * </ul>
     *
     * @param itemObj 文章 JSON 对象
     * @return 新闻条目，解析失败返回 null
     */
    private NewsItem parseArticleItem(JSONObject itemObj) {
        String id = itemObj.getStr("id");
        String title = itemObj.getStr("title");

        // 标题必须存在
        if (StrUtil.isBlank(title)) {
            return null;
        }

        // desc 字段作为摘要
        String intro = itemObj.getStr("desc");

        // media_info.chl_name 作为来源
        String source = "腾讯新闻";
        JSONObject mediaInfo = itemObj.getJSONObject("media_info");
        if (mediaInfo != null && StrUtil.isNotBlank(mediaInfo.getStr("chl_name"))) {
            source = mediaInfo.getStr("chl_name");
        }

        // publish_time 作为发布时间
        String time = itemObj.getStr("publish_time");

        // link_info.url 作为链接
        String url = "";
        JSONObject linkInfo = itemObj.getJSONObject("link_info");
        if (linkInfo != null) {
            url = StrUtil.blankToDefault(linkInfo.getStr("url"), "");
        }

        // pic_info.big_img[0] 作为封面图片
        String img = "";
        JSONObject picInfo = itemObj.getJSONObject("pic_info");
        if (picInfo != null) {
            JSONArray bigImgs = picInfo.getJSONArray("big_img");
            if (bigImgs != null && !bigImgs.isEmpty()) {
                img = bigImgs.getStr(0);
            }
        }

        return new NewsItem(id, title, intro, source, time, url, img, null, null);
    }

    /**
     * 功能：通过 HTML 解析方式获取数据。
     *
     * @param category 新闻分类
     * @return API 响应实体
     */
    protected TencentNewsResponse fetchFromHtml(NewsCategory category) {
        try {
            String pageUrl = "https://news.qq.com/tag/" + category.getTagId();

            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", USER_AGENT);
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

            HttpResponse httpResponse = HttpRequest.get(pageUrl)
                    .headerMap(headers, false)
                    .timeout(10000)
                    .execute();

            if (httpResponse.isOk()) {
                String html = httpResponse.body();
                if (StrUtil.isNotBlank(html)) {
                    return parseHtmlResponse(html);
                }
            }
        } catch (Exception e) {
            log.error("HTML 解析方式采集失败", e);
        }

        return null;
    }

    /**
     * 功能：解析 HTML 页面中的新闻数据。
     *
     * @param html HTML 内容
     * @return 解析后的响应对象
     */
    protected TencentNewsResponse parseHtmlResponse(String html) {
        List<NewsItem> items = new ArrayList<>();

        try {
            // 尝试从 HTML 中提取 JSON 数据
            String pattern = "\"info\"\\s*:\\s*\\[";
            int startIndex = html.indexOf(pattern);
            if (startIndex == -1) {
                log.warn("HTML 中未找到新闻数据");
                return new TencentNewsResponse(0, "success", new TencentNewsResponse.Data(items));
            }

            // 找到 info 数组的起始位置
            int arrayStart = html.indexOf("[", startIndex);
            if (arrayStart == -1) {
                return new TencentNewsResponse(0, "success", new TencentNewsResponse.Data(items));
            }

            // 手动查找匹配的括号
            int bracketCount = 0;
            int arrayEnd = -1;
            for (int i = arrayStart; i < html.length(); i++) {
                char c = html.charAt(i);
                if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    bracketCount--;
                    if (bracketCount == 0) {
                        arrayEnd = i + 1;
                        break;
                    }
                }
            }

            if (arrayEnd == -1) {
                return new TencentNewsResponse(0, "success", new TencentNewsResponse.Data(items));
            }

            String jsonArrayStr = html.substring(arrayStart, arrayEnd);
            JSONArray infoArray = JSONUtil.parseArray(jsonArrayStr);

            for (int i = 0; i < infoArray.size() && items.size() < 30; i++) {
                JSONObject itemObj = infoArray.getJSONObject(i);
                if (itemObj == null) {
                    continue;
                }

                NewsItem item = new NewsItem(
                        itemObj.getStr("id"),
                        itemObj.getStr("title"),
                        itemObj.getStr("intro"),
                        itemObj.getStr("source"),
                        itemObj.getStr("time"),
                        itemObj.getStr("url"),
                        itemObj.getStr("img"),
                        itemObj.getStr("channel"),
                        itemObj.getStr("tag")
                );
                items.add(item);
            }
        } catch (Exception e) {
            log.warn("解析 HTML 中的 JSON 数据失败", e);
        }

        return new TencentNewsResponse(0, "success", new TencentNewsResponse.Data(items));
    }

    /**
     * 功能：将 API 响应映射为统一的 {@link MorningNewsItem} 列表。
     *
     * @param response API 响应
     * @return 早报条目列表
     */
    protected List<MorningNewsItem> mapToItems(TencentNewsResponse response) {
        if (response == null || response.data() == null || response.data().info() == null) {
            log.warn("腾讯新闻 API 响应异常或为空");
            return Collections.emptyList();
        }

        List<NewsItem> newsItems = response.data().info();
        if (newsItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<MorningNewsItem> items = new ArrayList<>(newsItems.size());
        int rank = 1;
        for (NewsItem item : newsItems) {
            if (StrUtil.isNotBlank(item.title())) {
                items.add(toMorningNewsItem(item, rank++));
            }
        }
        return items;
    }

    /**
     * 功能：将单个 API 条目转换为 {@link MorningNewsItem}。
     *
     * @param item API 条目
     * @param rank 排名
     * @return 统一早报条目
     */
    protected MorningNewsItem toMorningNewsItem(NewsItem item, int rank) {
        String title = StrUtil.blankToDefault(item.title(), "");
        String summary = StrUtil.blankToDefault(item.intro(), "");
        String source = StrUtil.blankToDefault(item.source(), "腾讯新闻");
        String publishTime = StrUtil.blankToDefault(item.time(), "");
        String url = StrUtil.blankToDefault(item.url(), "");
        String coverImage = StrUtil.blankToDefault(item.img(), "");

        return new MorningNewsItem(rank, title, summary, source, publishTime, url, coverImage);
    }
}
