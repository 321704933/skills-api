package ai.skills.api.fortune.controller;

import ai.skills.api.fortune.model.AlmanacResult;
import ai.skills.api.fortune.service.AlmanacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/13
 * 功能：今日黄历接口控制器
 * 作者：Devil
 */
@Tag(name = "今日黄历")
@RestController
@RequestMapping("/api/v1/almanac")
@RequiredArgsConstructor
public class AlmanacController {

    private final AlmanacService almanacService;

    /**
     * 功能：获取今日黄历（基于真实老黄历数据）
     *
     * @return 黄历信息
     */
    @Operation(summary = "获取今日黄历", description = "获取今日黄历信息，包含干支、生肖、宜忌、吉凶方位等")
    @GetMapping("/almanac")
    public AlmanacResult getAlmanac() {
        return almanacService.getAlmanac();
    }
}
