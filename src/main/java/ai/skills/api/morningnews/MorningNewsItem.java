package ai.skills.api.morningnews;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建时间：2026/03/17
 * 功能：单条早报数据，包含排名、标题、摘要、来源、发布时间和链接。
 * 作者：Devil
 */
@Schema(name = "早报条目", description = "单条早报数据")
public record MorningNewsItem(
        @Schema(description = "排名", example = "1")
        int rank,

        @Schema(description = "标题", example = "今日要闻")
        String title,

        @Schema(description = "摘要", example = "这是一条重要新闻的摘要")
        String summary,

        @Schema(description = "来源", example = "腾讯新闻")
        String source,

        @Schema(description = "发布时间", example = "2026-03-17 08:00")
        String publishTime,

        @Schema(description = "链接地址")
        String url,

        @Schema(description = "封面图片")
        String coverImage
) {
}
