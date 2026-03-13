package ai.skills.api.fortune.controller;

import ai.skills.api.fortune.model.AlmanacResult;
import ai.skills.api.fortune.service.AlmanacService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/13
 * 功能：今日黄历接口控制器
 * 作者：Devil
 */
@RestController
@RequestMapping("/api")
public class AlmanacController {

    private final AlmanacService almanacService;

    public AlmanacController(AlmanacService almanacService) {
        this.almanacService = almanacService;
    }

    /**
     * 功能：获取今日黄历（基于真实老黄历数据）
     *
     * @return 黄历信息
     */
    @GetMapping("/almanac")
    public AlmanacResult getAlmanac() {
        return almanacService.getAlmanac();
    }
}
