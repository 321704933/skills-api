package ai.skills.api.weather.controller;

import ai.skills.api.common.api.ResponseCode;
import ai.skills.api.common.exception.BizException;
import ai.skills.api.common.ratelimit.RateLimited;
import ai.skills.api.weather.collector.WeatherCollector;
import ai.skills.api.weather.model.WeatherResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气预报接口
 *
 * @author Devil
 * @since 2025-03-16
 */
@Tag(name = "天气预报", description = "天气预报查询接口，包含实时天气、空气质量、7天预报、逐小时预报、24小时观测和生活指数")
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherCollector weatherCollector;

    /**
     * 根据城市名称查询完整天气数据
     *
     * @param city 城市名称
     * @return 完整天气数据（实时天气 + 空气质量 + 7天预报 + 24小时观测）
     */
    @Operation(summary = "查询天气预报", description = "根据城市名称查询完整天气数据，包含实时天气、空气质量、7天预报（含逐小时预报和生活指数）、过去24小时观测数据")
    @RateLimited(permits = 30, windowSeconds = 60, keyPrefix = "weather")
    @GetMapping("/{city}")
    public WeatherResult getWeather(
            @Parameter(description = "城市名称", example = "北京")
            @PathVariable String city) {
        WeatherResult weather = weatherCollector.collect(city);
        if (weather == null) {
            throw new BizException(ResponseCode.BIZ_ERROR, "未找到城市: " + city);
        }
        return weather;
    }
}
