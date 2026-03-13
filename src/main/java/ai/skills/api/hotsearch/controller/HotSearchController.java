package ai.skills.api.hotsearch.controller;

import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.entity.HotSearchRecord;
import ai.skills.api.hotsearch.service.HotSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：热搜数据查询接口，提供最新热搜和历史记录查询。
 * 作者：Devil
 */
@Tag(name = "热搜数据")
@RestController
@RequestMapping("/api/v1/hot-search")
public class HotSearchController {

    private final HotSearchService hotSearchService;

    public HotSearchController(HotSearchService hotSearchService) {
        this.hotSearchService = hotSearchService;
    }

    /**
     * 功能：获取指定平台最新一批热搜数据（优先 Redis 缓存，缓存未命中回源数据库）。
     *
     * @param platform 平台标识（baidu / weibo / douyin / toutiao）
     * @return 最新热搜结果
     */
    @Operation(summary = "获取最新热搜", description = "获取指定平台最新的热搜数据，优先从Redis缓存获取")
    @GetMapping("/{platform}/latest")
    public HotSearchResult latest(@PathVariable String platform) {
        return hotSearchService.getLatest(platform);
    }

    /**
     * 功能：分页查询指定平台的历史热搜记录。
     *
     * @param platform 平台标识
     * @param limit    每页条数（默认 50）
     * @param offset   偏移量（默认 0）
     * @return 热搜记录列表
     */
    @Operation(summary = "查询历史热搜", description = "分页查询指定平台的历史热搜记录")
    @GetMapping("/{platform}/history")
    public List<HotSearchRecord> history(
            @PathVariable String platform,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return hotSearchService.getHistory(platform, limit, offset);
    }
}
