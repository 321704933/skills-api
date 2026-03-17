package ai.skills.api.morningnews.collector;

import ai.skills.api.morningnews.NewsCategory;
import org.springframework.stereotype.Component;

/**
 * 创建时间：2026/03/17
 * 功能：游戏早报采集器，通过腾讯新闻 API 获取游戏早报数据。
 * <p>
 * Bean 名称 {@code "game"} 与 YAML 中 {@code skills-api.morning-news.platforms.game} 对应。
 * 作者：Devil
 */
@Component("game")
public class GameMorningNewsCollector extends AbstractTencentNewsCollector {

    @Override
    protected NewsCategory getCategory() {
        return NewsCategory.GAME;
    }
}
