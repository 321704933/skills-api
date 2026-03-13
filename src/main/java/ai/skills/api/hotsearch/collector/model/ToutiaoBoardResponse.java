package ai.skills.api.hotsearch.collector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：映射今日头条热榜 API（{@code /hot-event/hot-board/}）的完整 JSON 响应结构。
 * 作者：Devil
 *
 * @param data 热榜条目列表
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ToutiaoBoardResponse(List<HotItem> data) {

    /**
     * 功能：映射今日头条 API 返回的单条热榜数据。
     *
     * @param title        热榜标题
     * @param hotValue     热度值（字符串形式的数字）
     * @param url          热榜详情链接
     * @param label        标签（置顶/热/新/独家等，可为空）
     * @param clusterIdStr 聚合 ID（字符串）
     * @param clusterType  聚合类型
     * @param queryWord    搜索关键词
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HotItem(
            @JsonProperty("Title") String title,
            @JsonProperty("HotValue") String hotValue,
            @JsonProperty("Url") String url,
            @JsonProperty("Label") String label,
            @JsonProperty("ClusterIdStr") String clusterIdStr,
            @JsonProperty("ClusterType") Integer clusterType,
            @JsonProperty("QueryWord") String queryWord
    ) {
    }
}
