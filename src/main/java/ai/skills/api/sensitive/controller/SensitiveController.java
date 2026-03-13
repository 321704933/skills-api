package ai.skills.api.sensitive.controller;

import ai.skills.api.sensitive.model.SensitiveCheckRequest;
import ai.skills.api.sensitive.model.SensitiveCheckResult;
import ai.skills.api.sensitive.service.SensitiveWordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词检测接口。
 * 作者：Devil
 */
@RestController
@RequestMapping("/api/v1/sensitive")
public class SensitiveController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    /**
     * 功能：检测文本中是否包含违禁词。
     *
     * @param request 包含待检测文本的请求体
     * @return 检测结果
     */
    @PostMapping("/check")
    public SensitiveCheckResult check(@Valid @RequestBody SensitiveCheckRequest request) {
        return sensitiveWordService.check(request.text());
    }
}
