package ai.skills.api.hotsearch.collector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：映射微博热搜榜 API（{@code /ajax/side/searchBand?type=hot}）的完整 JSON 响应结构。
 * 作者：Devil
 *
 * @param ok   状态码（1 表示成功）
 * @param data 业务数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeiboBoardResponse(int ok, WeiboBoardData data) {

    /**
     * 功能：承载 {@code data} 层级，包含实时热搜列表。
     *
     * @param realtime 实时热搜列表
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeiboBoardData(List<HotItem> realtime) {
    }

    /**
     * 功能：映射微博 API 返回的单条热搜数据。
     *
     * @param rank        排名（0-based）
     * @param realpos     实际展示位置（1-based）
     * @param word        热搜关键词
     * @param note        热搜说明（通常与 word 相同）
     * @param num         热度值（搜索量）
     * @param labelName   标签名称（新/热/沸/爆）
     * @param iconDesc    图标描述（新/热/沸/爆）
     * @param flag        标记类型（1=新）
     * @param wordScheme  话题格式（#xxx#）
     * @param topicFlag   是否为话题
     * @param emoticon    表情符号
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HotItem(
            Integer rank,
            @JsonProperty("realpos") Integer realpos,
            String word,
            String note,
            Long num,
            @JsonProperty("label_name") String labelName,
            @JsonProperty("icon_desc") String iconDesc,
            Integer flag,
            @JsonProperty("word_scheme") String wordScheme,
            @JsonProperty("topic_flag") Integer topicFlag,
            String emoticon
    ) {
    }
}
