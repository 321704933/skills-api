package ai.skills.api.prose.controller;

import ai.skills.api.prose.entity.ProseSentence;
import ai.skills.api.prose.service.ProseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/13
 * 功能：散文句子查询接口，提供随机句子查询。
 * 作者：Devil
 */
@RestController
@RequestMapping("/api/v1/prose")
public class ProseController {

    private final ProseService proseService;

    public ProseController(ProseService proseService) {
        this.proseService = proseService;
    }

    /**
     * 功能：随机返回一条散文句子。
     *
     * @return 随机句子
     */
    @GetMapping("/random")
    public ProseSentence random() {
        return proseService.getRandomSentence();
    }
}
