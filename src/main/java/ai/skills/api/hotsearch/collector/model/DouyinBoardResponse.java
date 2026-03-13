package ai.skills.api.hotsearch.collector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：映射抖音热搜榜 API（{@code /aweme/v1/web/hot/search/list/}）的完整 JSON 响应结构。
 * 作者：Devil
 *
 * @param statusCode 状态码（0 表示成功）
 * @param data       业务数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DouyinBoardResponse(
        @JsonProperty("status_code") int statusCode,
        DouyinBoardData data
) {

    /**
     * 功能：承载 {@code data} 层级，包含热搜词列表和上升趋势列表。
     *
     * @param wordList     热搜词列表（主榜单，约 50 条）
     * @param trendingList 实时上升热点列表
     * @param activeTime   榜单更新时间
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DouyinBoardData(
            @JsonProperty("word_list") List<HotItem> wordList,
            @JsonProperty("trending_list") List<HotItem> trendingList,
            @JsonProperty("active_time") String activeTime
    ) {
    }

    /**
     * 功能：映射抖音 API 返回的单条热搜数据。
     *
     * @param position    排名（1-based）
     * @param word        热搜关键词
     * @param hotValue    热度值
     * @param label       标签编码（0=无, 1=新, 3=热, 5=独家, 8=推荐）
     * @param sentenceId  热搜句子 ID
     * @param groupId     事件分组 ID
     * @param eventTime   事件时间戳（秒）
     * @param videoCount  相关视频数
     * @param wordType    词类型
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HotItem(
            Integer position,
            String word,
            @JsonProperty("hot_value") Long hotValue,
            Integer label,
            @JsonProperty("sentence_id") String sentenceId,
            @JsonProperty("group_id") String groupId,
            @JsonProperty("event_time") Long eventTime,
            @JsonProperty("video_count") Integer videoCount,
            @JsonProperty("word_type") Integer wordType
    ) {
    }
}
