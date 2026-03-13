package ai.skills.api.hotsearch;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建时间：2026/03/13
 * 功能：单条热搜数据，包含排名、标题、热度值、链接和标签。
 * 作者：Devil
 */
@Schema(name = "热搜条目", description = "单条热搜数据")
public record HotSearchItem(
        @Schema(description = "排名", example = "1")
        int rank,

        @Schema(description = "标题", example = "今日热搜排名第一")
        String title,

        @Schema(description = "热度值", example = "9800000")
        long hotScore,

        @Schema(description = "链接地址")
        String url,

        @Schema(description = "热搜标签", example = "热")
        String hotTag
) {
}
