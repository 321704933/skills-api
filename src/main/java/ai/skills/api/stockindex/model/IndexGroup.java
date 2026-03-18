package ai.skills.api.stockindex.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 创建时间：2026/03/18
 * 功能：股票指数分组枚举，定义各大分组标识和中文名称。
 * 作者：Devil
 */
@Getter
@RequiredArgsConstructor
public enum IndexGroup {

    A_SHARE("a-share", "A股指数"),
    HK("hk", "港股指数"),
    US("us", "美股指数");

    /**
     * 分组标识
     */
    private final String code;

    /**
     * 分组中文名称
     */
    private final String name;

    /**
     * 功能：根据分组标识获取枚举值。
     *
     * @param code 分组标识
     * @return 枚举值，未找到返回 null
     */
    public static IndexGroup fromCode(String code) {
        for (IndexGroup group : values()) {
            if (group.getCode().equals(code)) {
                return group;
            }
        }
        return null;
    }
}
