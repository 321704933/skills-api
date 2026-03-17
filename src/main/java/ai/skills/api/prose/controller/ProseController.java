package ai.skills.api.prose.controller;

import ai.skills.api.prose.model.ProseSentenceResult;
import ai.skills.api.prose.service.ProseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/13
 * 功能：散文句子查询接口，提供随机句子查询。
 * 作者：Devil
 */
@Tag(name = "散文句子")
@RestController
@RequestMapping("/api/v1/prose")
@RequiredArgsConstructor
public class ProseController {

    private final ProseService proseService;


    /**
     * 功能：随机返回一条散文句子。
     *
     * @return 随机句子
     */
    @Operation(summary = "随机句子", description = "随机返回一条散文句子")
    @GetMapping("/random")
    public ProseSentenceResult random() {
        return proseService.getRandomSentence();
    }
}
