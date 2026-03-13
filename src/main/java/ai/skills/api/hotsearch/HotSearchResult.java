package ai.skills.api.hotsearch;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：热搜采集结果，包含平台标识、热搜列表和采集时间。
 * 作者：Devil
 *
 * @param platform    平台标识（如 baidu、weibo）
 * @param items       热搜条目列表
 * @param collectedAt 采集时间
 */
public record HotSearchResult(
        String platform,
        List<HotSearchItem> items,
        LocalDateTime collectedAt
) {
}
