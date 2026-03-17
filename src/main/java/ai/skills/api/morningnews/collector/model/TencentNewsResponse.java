package ai.skills.api.morningnews.collector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 创建时间：2026/03/17
 * 功能：映射腾讯新闻早报 API 的 JSON 响应结构。
 * 作者：Devil
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TencentNewsResponse(
        Integer ret,
        String msg,
        Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            List<NewsItem> info
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewsItem(
            @JsonProperty("id")
            String id,

            @JsonProperty("title")
            String title,

            @JsonProperty("intro")
            String intro,

            @JsonProperty("source")
            String source,

            @JsonProperty("time")
            String time,

            @JsonProperty("url")
            String url,

            @JsonProperty("img")
            String img,

            @JsonProperty("channel")
            String channel,

            @JsonProperty("tag")
            String tag
    ) {
    }
}
