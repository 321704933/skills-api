package ai.skills.api.hotsearch.collector.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 创建时间：2026/03/17
 * 功能：Bilibili 热门视频 API 响应模型，映射热门视频列表数据。
 * <p>
 * API 地址：{@code https://api.bilibili.com/x/web-interface/popular?ps=50}
 * 作者：Devil
 */
public record BilibiliPopularResponse(
        int code,
        String message,
        Data data
) {

    /**
     * 功能：判断 API 响应是否成功。
     *
     * @return code == 0 时返回 true
     */
    public boolean success() {
        return code == 0;
    }

    /**
     * 功能：响应数据容器
     */
    public record Data(List<VideoItem> list) {
    }

    /**
     * 功能：视频条目信息
     */
    public record VideoItem(
            long aid,
            String bvid,
            String title,
            String pic,
            Owner owner,
            Stat stat,
            @JsonProperty("short_link_v2")
            String shortLinkV2
    ) {
    }

    /**
     * 功能：UP 主信息
     */
    public record Owner(long mid, String name) {
    }

    /**
     * 功能：视频统计数据
     */
    public record Stat(
            long view,       // 播放量
            long like,       // 点赞数
            long coin,       // 投币数
            long share,      // 分享数
            long favorite    // 收藏数
    ) {
    }
}
