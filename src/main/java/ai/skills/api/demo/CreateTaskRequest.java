package ai.skills.api.demo;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建时间：2026/03/12
 * 功能：定义创建采集任务的请求体。
 * 作者：Devil
 *
 * @param source 数据来源
 * @param keyword 采集关键词
 */
public record CreateTaskRequest(
        @NotBlank(message = "source 不能为空")
        String source,
        @NotBlank(message = "keyword 不能为空")
        String keyword
) {
}
