package ai.skills.api.fortune.controller;

import ai.skills.api.fortune.model.FortuneResult;
import ai.skills.api.fortune.service.FortuneService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/13
 * 功能：每日运势查询接口。
 * 作者：Devil
 */
@RestController
@RequestMapping("/api/v1/fortune")
public class FortuneController {

    private final FortuneService fortuneService;

    public FortuneController(FortuneService fortuneService) {
        this.fortuneService = fortuneService;
    }

    /**
     * 功能：获取每日运势信息。
     *
     * @param date         可选，日期 (yyyy-MM-dd)，不传则默认今天
     * @param constellation 可选，星座名称，不传则根据日期自动计算
     * @return 运势信息（农历、干支、宜忌、星座运势等）
     */
    @GetMapping("/daily")
    public FortuneResult daily(@RequestParam(required = false) String date,
                               @RequestParam(required = false) String constellation) {
        return fortuneService.getFortune(date, constellation);
    }
}
