package ai.skills.api.sensitive.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词检测请求体。
 * 作者：Devil
 *
 * @param text 待检测文本
 */
public record SensitiveCheckRequest(
        @NotBlank(message = "text 不能为空")
        String text
) {
}
