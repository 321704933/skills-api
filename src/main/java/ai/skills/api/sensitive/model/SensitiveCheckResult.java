package ai.skills.api.sensitive.model;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词检测结果。
 * 作者：Devil
 *
 * @param hasSensitive 是否包含违禁词
 * @param foundWords   命中的违禁词列表（去重）
 * @param filteredText 违禁词替换为 * 后的文本
 */
public record SensitiveCheckResult(
        boolean hasSensitive,
        List<String> foundWords,
        String filteredText
) {
}
