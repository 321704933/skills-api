package ai.skills.api.hotsearch.controller;

import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.Platform;
import ai.skills.api.hotsearch.service.HotSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 创建时间：2026/03/13
 * 功能：热搜数据查询接口，提供最新热搜和历史记录查询。
 * 作者：Devil
 */
@Tag(name = "热搜数据")
@RestController
public class HotSearchController {

    private final HotSearchService hotSearchService;

    public HotSearchController(HotSearchService hotSearchService) {
        this.hotSearchService = hotSearchService;
    }

    // ==================== 百度热搜 ====================

    /**
     * 获取百度最新热搜
     */
    @Operation(summary = "百度最新热搜", description = "获取百度平台最新的热搜数据")
    @GetMapping("/api/v1/hot-search/baidu/latest")
    public HotSearchResult baiduLatest() {
        return hotSearchService.getLatest(Platform.BAIDU);
    }

    /**
     * 查询百度历史热搜
     */
    @Operation(summary = "百度历史热搜", description = "查询百度平台指定日期的热搜记录")
    @GetMapping("/api/v1/hot-search/baidu/history")
    public HotSearchResult baiduHistory(
            @Parameter(description = "查询日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return hotSearchService.getByDate(Platform.BAIDU, date);
    }

    // ==================== 微博热搜 ====================

    /**
     * 获取微博最新热搜
     */
    @Operation(summary = "微博最新热搜", description = "获取微博平台最新的热搜数据")
    @GetMapping("/api/v1/hot-search/weibo/latest")
    public HotSearchResult weiboLatest() {
        return hotSearchService.getLatest(Platform.WEIBO);
    }

    /**
     * 查询微博历史热搜
     */
    @Operation(summary = "微博历史热搜", description = "查询微博平台指定日期的热搜记录")
    @GetMapping("/api/v1/hot-search/weibo/history")
    public HotSearchResult weiboHistory(
            @Parameter(description = "查询日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return hotSearchService.getByDate(Platform.WEIBO, date);
    }

    // ==================== 抖音热搜 ====================

    /**
     * 获取抖音最新热搜
     */
    @Operation(summary = "抖音最新热搜", description = "获取抖音平台最新的热搜数据")
    @GetMapping("/api/v1/hot-search/douyin/latest")
    public HotSearchResult douyinLatest() {
        return hotSearchService.getLatest(Platform.DOUYIN);
    }

    /**
     * 查询抖音历史热搜
     */
    @Operation(summary = "抖音历史热搜", description = "查询抖音平台指定日期的热搜记录")
    @GetMapping("/api/v1/hot-search/douyin/history")
    public HotSearchResult douyinHistory(
            @Parameter(description = "查询日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return hotSearchService.getByDate(Platform.DOUYIN, date);
    }

    // ==================== 今日头条热搜 ====================

    /**
     * 获取今日头条最新热搜
     */
    @Operation(summary = "今日头条最新热搜", description = "获取今日头条平台最新的热搜数据")
    @GetMapping("/api/v1/hot-search/toutiao/latest")
    public HotSearchResult toutiaoLatest() {
        return hotSearchService.getLatest(Platform.TOUTIAO);
    }

    /**
     * 查询今日头条历史热搜
     */
    @Operation(summary = "今日头条历史热搜", description = "查询今日头条平台指定日期的热搜记录")
    @GetMapping("/api/v1/hot-search/toutiao/history")
    public HotSearchResult toutiaoHistory(
            @Parameter(description = "查询日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return hotSearchService.getByDate(Platform.TOUTIAO, date);
    }
}
