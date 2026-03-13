package ai.skills.api.hotsearch;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：热搜采集结果，包含平台标识、热搜列表和采集时间。
 * 作者：Devil
 */
@Schema(name = "热搜采集结果", description = "热搜采集结果详情")
public record HotSearchResult(
        @Schema(description = "平台标识", example = "baidu")
        String platform,

        @Schema(description = "热搜条目列表")
        List<HotSearchItem> items,

        @Schema(description = "采集时间")
        LocalDateTime collectedAt
) {
}
