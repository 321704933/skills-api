package ai.skills.api.sensitive.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词检测结果。
 * 作者：Devil
 */
@Schema(name = "违禁词检测结果", description = "违禁词检测结果详情")
public record SensitiveCheckResult(
        @Schema(description = "是否包含违禁词", example = "true")
        boolean hasSensitive,

        @Schema(description = "命中的违禁词列表")
        List<String> foundWords,

        @Schema(description = "违禁词替换为 * 后的文本")
        String filteredText
) {
}
