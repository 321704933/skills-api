package ai.skills.api.morningnews.collector;

import ai.skills.api.morningnews.NewsCategory;
import org.springframework.stereotype.Component;

/**
 * 创建时间：2026/03/17
 * 功能：国际早报采集器，通过腾讯新闻 API 获取国际早报数据。
 * <p>
 * Bean 名称 {@code "international"} 与 YAML 中 {@code skills-api.morning-news.platforms.international} 对应。
 * 作者：Devil
 */
@Component("international")
public class InternationalMorningNewsCollector extends AbstractTencentNewsCollector {

    @Override
    protected NewsCategory getCategory() {
        return NewsCategory.INTERNATIONAL;
    }
}
