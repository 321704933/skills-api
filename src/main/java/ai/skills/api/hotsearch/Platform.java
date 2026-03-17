package ai.skills.api.hotsearch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 创建时间：2026/03/16
 * 功能：热搜平台枚举
 * 作者：Devil
 */
@Getter
@RequiredArgsConstructor
public enum Platform {

    BAIDU("baidu", "百度"),
    WEIBO("weibo", "微博"),
    DOUYIN("douyin", "抖音"),
    TOUTIAO("toutiao", "今日头条"),
    BILIBILI("bilibili", "哔哩哔哩");

    /**
     * 平台标识
     */
    private final String code;

    /**
     * 平台名称
     */
    private final String name;

    /**
     * 根据标识获取枚举
     *
     * @param code 平台标识
     * @return 枚举值，未找到返回 null
     */
    public static Platform fromCode(String code) {
        for (Platform platform : values()) {
            if (platform.getCode().equals(code)) {
                return platform;
            }
        }
        return null;
    }
}
