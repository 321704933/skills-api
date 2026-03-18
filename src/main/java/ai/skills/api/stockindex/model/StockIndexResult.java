package ai.skills.api.stockindex.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建时间：2026/03/18
 * 功能：股票指数查询结果，包含指数列表和采集时间。
 * 作者：Devil
 */
@Schema(name = "股票指数查询结果", description = "股票指数查询结果详情")
public record StockIndexResult(

        @Schema(description = "分组标识", example = "a-share")
        String group,

        @Schema(description = "分组名称", example = "A股指数")
        String groupName,

        @Schema(description = "指数行情列表")
        List<IndexQuote> quotes,

        @Schema(description = "数据采集时间")
        LocalDateTime collectedAt
) {
}
