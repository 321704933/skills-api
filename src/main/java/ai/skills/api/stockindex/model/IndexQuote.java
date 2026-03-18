package ai.skills.api.stockindex.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建时间：2026/03/18
 * 功能：单个股票/指数的实时行情数据。
 * 作者：Devil
 */
@Schema(name = "指数行情", description = "单个股票/指数的实时行情数据")
public record IndexQuote(

        @Schema(description = "指数代码", example = "000001")
        String code,

        @Schema(description = "指数名称", example = "上证指数")
        String name,

        @Schema(description = "当前价格", example = "4034.01")
        String currentPrice,

        @Schema(description = "涨跌额", example = "-15.9")
        String priceChange,

        @Schema(description = "涨跌幅（%）", example = "-0.39")
        String changePercent,

        @Schema(description = "成交额（元）", example = "635300591805.9")
        String turnover,

        @Schema(description = "上涨家数", example = "1090")
        String upCount,

        @Schema(description = "下跌家数", example = "1206")
        String downCount,

        @Schema(description = "平盘家数", example = "48")
        String flatCount,

        @Schema(description = "数据日期", example = "2026-03-18")
        String date,

        @Schema(description = "数据时间", example = "13:22:31")
        String time
) {
}
