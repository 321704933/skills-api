package ai.skills.api.hotsearch;

/**
 * 创建时间：2026/03/13
 * 功能：单条热搜数据，包含排名、标题、热度值、链接和标签。
 * 作者：Devil
 *
 * @param rank     排名
 * @param title    标题
 * @param hotScore 热度值
 * @param url      链接地址
 * @param hotTag   热搜标签（热/新/沸等，可为空）
 */
public record HotSearchItem(
        int rank,
        String title,
        long hotScore,
        String url,
        String hotTag
) {
}
