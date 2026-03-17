package ai.skills.api.morningnews.controller;

import ai.skills.api.morningnews.MorningNewsResult;
import ai.skills.api.morningnews.NewsCategory;
import ai.skills.api.morningnews.service.MorningNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/17
 * 功能：早报数据查询接口，提供各分类最新早报查询。
 * 作者：Devil
 */
@Tag(name = "早报数据")
@RestController
@RequestMapping("/api/v1/morning-news")
@RequiredArgsConstructor
public class MorningNewsController {

    private final MorningNewsService morningNewsService;

    /**
     * 获取综合最新早报
     */
    @Operation(summary = "综合最新早报", description = "获取综合分类最新的早报数据")
    @GetMapping("/general/latest")
    public MorningNewsResult generalLatest() {
        return morningNewsService.getLatest(NewsCategory.GENERAL);
    }

    /**
     * 获取财经最新早报
     */
    @Operation(summary = "财经最新早报", description = "获取财经分类最新的早报数据")
    @GetMapping("/finance/latest")
    public MorningNewsResult financeLatest() {
        return morningNewsService.getLatest(NewsCategory.FINANCE);
    }

    /**
     * 获取科技最新早报
     */
    @Operation(summary = "科技最新早报", description = "获取科技分类最新的早报数据")
    @GetMapping("/tech/latest")
    public MorningNewsResult techLatest() {
        return morningNewsService.getLatest(NewsCategory.TECH);
    }

    /**
     * 获取体育最新早报
     */
    @Operation(summary = "体育最新早报", description = "获取体育分类最新的早报数据")
    @GetMapping("/sports/latest")
    public MorningNewsResult sportsLatest() {
        return morningNewsService.getLatest(NewsCategory.SPORTS);
    }

    /**
     * 获取国际最新早报
     */
    @Operation(summary = "国际最新早报", description = "获取国际分类最新的早报数据")
    @GetMapping("/international/latest")
    public MorningNewsResult internationalLatest() {
        return morningNewsService.getLatest(NewsCategory.INTERNATIONAL);
    }

    /**
     * 获取汽车最新早报
     */
    @Operation(summary = "汽车最新早报", description = "获取汽车分类最新的早报数据")
    @GetMapping("/auto/latest")
    public MorningNewsResult autoLatest() {
        return morningNewsService.getLatest(NewsCategory.AUTO);
    }

    /**
     * 获取游戏最新早报
     */
    @Operation(summary = "游戏最新早报", description = "获取游戏分类最新的早报数据")
    @GetMapping("/game/latest")
    public MorningNewsResult gameLatest() {
        return morningNewsService.getLatest(NewsCategory.GAME);
    }
}
