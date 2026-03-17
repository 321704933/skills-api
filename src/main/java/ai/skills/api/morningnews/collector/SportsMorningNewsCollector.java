package ai.skills.api.morningnews.collector;

import ai.skills.api.morningnews.NewsCategory;
import org.springframework.stereotype.Component;

/**
 * 创建时间：2026/03/17
 * 功能：体育早报采集器，通过腾讯新闻 API 获取体育早报数据。
 * <p>
 * Bean 名称 {@code "sports"} 与 YAML 中 {@code skills-api.morning-news.platforms.sports} 对应。
 * 作者：Devil
 */
@Component("sports")
public class SportsMorningNewsCollector extends AbstractTencentNewsCollector {

    @Override
    protected NewsCategory getCategory() {
        return NewsCategory.SPORTS;
    }
}
