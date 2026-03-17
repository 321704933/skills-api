package ai.skills.api.morningnews;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建时间：2026/03/17
 * 功能：早报采集结果，包含分类标识、早报列表和采集时间。
 * 作者：Devil
 */
@Schema(name = "早报采集结果", description = "早报采集结果详情")
public record MorningNewsResult(
        @Schema(description = "分类标识", example = "general")
        String category,

        @Schema(description = "分类名称", example = "综合早报")
        String categoryName,

        @Schema(description = "早报条目列表")
        List<MorningNewsItem> items,

        @Schema(description = "采集时间")
        LocalDateTime collectedAt
) {
}
