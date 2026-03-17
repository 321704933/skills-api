package ai.skills.api.hotsearch.controller;

import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.Platform;
import ai.skills.api.hotsearch.service.HotSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/13
 * 功能：热搜数据查询接口，提供各平台最新热搜查询。
 * 作者：Devil
 */
@Tag(name = "热搜数据")
@RestController
@RequestMapping("/api/v1/hot-search")
@RequiredArgsConstructor
public class HotSearchController {

    private final HotSearchService hotSearchService;

    /**
     * 获取百度最新热搜
     */
    @Operation(summary = "百度最新热搜", description = "获取百度平台最新的热搜数据")
    @GetMapping("/baidu/latest")
    public HotSearchResult baiduLatest() {
        return hotSearchService.getLatest(Platform.BAIDU);
    }

    /**
     * 获取微博最新热搜
     */
    @Operation(summary = "微博最新热搜", description = "获取微博平台最新的热搜数据")
    @GetMapping("/weibo/latest")
    public HotSearchResult weiboLatest() {
        return hotSearchService.getLatest(Platform.WEIBO);
    }

    /**
     * 获取抖音最新热搜
     */
    @Operation(summary = "抖音最新热搜", description = "获取抖音平台最新的热搜数据")
    @GetMapping("/douyin/latest")
    public HotSearchResult douyinLatest() {
        return hotSearchService.getLatest(Platform.DOUYIN);
    }

    /**
     * 获取今日头条最新热搜
     */
    @Operation(summary = "今日头条最新热搜", description = "获取今日头条平台最新的热搜数据")
    @GetMapping("/toutiao/latest")
    public HotSearchResult toutiaoLatest() {
        return hotSearchService.getLatest(Platform.TOUTIAO);
    }
}
