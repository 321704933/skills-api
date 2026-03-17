package ai.skills.api.prose.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建时间：2026/03/13
 * 功能：散文句子数据模型。
 * 作者：Devil
 */
@Schema(name = "散文句子结果", description = "散文句子数据")
@Data
public class ProseSentenceResult {

    /** 句子正文 */
    @Schema(description = "句子正文", example = "我在人间凑数的日子")
    private String content;

    /** 出处（书名/集名） */
    @Schema(description = "出处", example = "《我在人间凑数的日子》")
    private String source;
}
