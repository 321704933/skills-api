package ai.skills.api.hotsearch.collector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：映射百度热搜榜 API（{@code /api/board?platform=pc&tab=realtime}）的完整 JSON 响应结构。
 * 作者：Devil
 *
 * @param success 请求是否成功
 * @param data    业务数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BaiduBoardResponse(boolean success, BoardData data) {

    /**
     * 功能：承载 {@code data} 层级，包含多个 Card 卡片。
     *
     * @param cards 卡片列表
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BoardData(List<Card> cards) {
    }

    /**
     * 功能：承载单个 Card 卡片，{@code content} 为该卡片下的热搜条目列表。
     *
     * @param content 热搜条目列表
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(List<HotItem> content) {
    }

    /**
     * 功能：映射百度 API 返回的单条热搜数据。
     *
     * @param index     排名索引（0-based）
     * @param word      热搜关键词
     * @param query     搜索查询词
     * @param hotScore  热度值（字符串形式）
     * @param appUrl    百度搜索链接
     * @param url       备用链接
     * @param desc      热搜描述
     * @param img       配图地址
     * @param hotChange 热度变化趋势（same/up/down）
     * @param hotTag    热搜标签编码（0=无, 1=新, 2=沸, 3=热）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HotItem(
            Integer index,
            String word,
            String query,
            String hotScore,
            String appUrl,
            String url,
            String desc,
            String img,
            String hotChange,
            String hotTag
    ) {
    }
}
