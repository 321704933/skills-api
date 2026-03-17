package ai.skills.api.sensitive.model;

import lombok.Data;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词数据模型。
 * 作者：Devil
 */
@Data
public class SensitiveWordItem {

    /** 违禁词 */
    private String word;

    /** 分类（广告/暴力/诈骗/色情/赌博/违法/侮辱/辱骂/其他） */
    private String category;
}
