package ai.skills.api.morningnews;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 创建时间：2026/03/17
 * 功能：早报分类枚举
 * 作者：Devil
 */
@Getter
@RequiredArgsConstructor
public enum NewsCategory {

    GENERAL("general", "综合早报", "aEWqxLtdgmQ="),
    FINANCE("finance", "财经早报", "bEemwrpehGc="),
    TECH("tech", "科技早报", "a06owLtahQ=="),
    SPORTS("sports", "体育早报", "bEGmxrxZgWQ="),
    INTERNATIONAL("international", "国际早报", "aEWqx7JegGE="),
    AUTO("auto", "汽车早报", "aEWqx7JRhWQ="),
    GAME("game", "游戏早报", "aEOmxrNbg2c=");

    /**
     * 分类标识
     */
    private final String code;

    /**
     * 分类名称
     */
    private final String name;

    /**
     * 腾讯新闻 Tag ID
     */
    private final String tagId;

    /**
     * 根据标识获取枚举
     *
     * @param code 分类标识
     * @return 枚举值，未找到返回 null
     */
    public static NewsCategory fromCode(String code) {
        for (NewsCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }
}
