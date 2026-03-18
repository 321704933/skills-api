package ai.skills.api.stockindex.controller;

import ai.skills.api.common.ratelimit.RateLimited;
import ai.skills.api.stockindex.model.IndexGroup;
import ai.skills.api.stockindex.model.StockIndexResult;
import ai.skills.api.stockindex.service.StockIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 创建时间：2026/03/18
 * 功能：股票指数行情查询接口，提供全球主要指数的实时行情数据。
 * 数据来源：腾讯财经 API，5 分钟 Redis 缓存。
 * 作者：Devil
 */
@Tag(name = "股票指数", description = "全球主要股票指数实时行情查询接口")
@RestController
@RequestMapping("/api/v1/stock-index")
@RequiredArgsConstructor
public class StockIndexController {

    private final StockIndexService stockIndexService;

    /**
     * 获取所有已启用分组的指数行情
     */
    @Operation(summary = "全部指数行情", description = "获取所有已启用分组的股票指数实时行情数据")
    @RateLimited(permits = 30, windowSeconds = 60, keyPrefix = "stock-index")
    @GetMapping
    public List<StockIndexResult> getAll() {
        return stockIndexService.getAll();
    }

    /**
     * 获取 A 股指数行情
     */
    @Operation(summary = "A股指数", description = "获取上证指数、深证成指、创业板指等 A 股主要指数行情")
    @RateLimited(permits = 30, windowSeconds = 60, keyPrefix = "stock-index")
    @GetMapping("/a-share")
    public StockIndexResult getAShare() {
        return stockIndexService.getByGroup(IndexGroup.A_SHARE);
    }

    /**
     * 获取港股指数行情
     */
    @Operation(summary = "港股指数", description = "获取恒生指数、国企指数、恒生科技指数等港股主要指数行情")
    @RateLimited(permits = 30, windowSeconds = 60, keyPrefix = "stock-index")
    @GetMapping("/hk")
    public StockIndexResult getHongKong() {
        return stockIndexService.getByGroup(IndexGroup.HK);
    }

    /**
     * 获取美股指数行情
     */
    @Operation(summary = "美股指数", description = "获取道琼斯、纳斯达克、标普500 等美股主要指数行情")
    @RateLimited(permits = 30, windowSeconds = 60, keyPrefix = "stock-index")
    @GetMapping("/us")
    public StockIndexResult getUS() {
        return stockIndexService.getByGroup(IndexGroup.US);
    }
}
