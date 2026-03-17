package ai.skills.api.weather.controller;

import ai.skills.api.common.api.ResponseCode;
import ai.skills.api.common.exception.BizException;
import ai.skills.api.common.ratelimit.RateLimited;
import ai.skills.api.weather.collector.WeatherCollector;
import ai.skills.api.weather.model.WeatherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public WeatherResponse getWeather(
            @Parameter(description = "城市名称", example = "北京")
            @PathVariable String city) {
        WeatherResponse weather = weatherCollector.collect(city);
        if (weather == null) {
            throw new BizException(ResponseCode.BIZ_ERROR, "未找到城市: " + city);
        }
        return weather;
    }

    /**
     * 根据城市编码查询完整天气数据
     *
     * @param cityCode 城市编码
     * @return 完整天气数据（实时天气 + 空气质量 + 7天预报 + 24小时观测）
     */
    @Operation(summary = "根据城市编码查询天气预报", description = "根据城市编码查询完整天气数据，包含实时天气、空气质量、7天预报（含逐小时预报和生活指数）、过去24小时观测数据")
    @RateLimited(permits = 30, windowSeconds = 60, keyPrefix = "weather")
    @GetMapping("/code/{cityCode}")
    public WeatherResponse getWeatherByCode(
            @Parameter(description = "城市编码", example = "101010100")
            @PathVariable String cityCode) {
        WeatherResponse weather = weatherCollector.collectByCode(cityCode, cityCode);
        if (weather == null) {
            throw new BizException(ResponseCode.BIZ_ERROR, "获取天气数据失败");
        }
        return weather;
    }

    /**
     * 搜索城市
     *
     * @param keyword 关键词
     * @return 匹配的城市列表
     */
    @Operation(summary = "搜索城市", description = "根据关键词搜索城市名称")
    @RateLimited(permits = 60, windowSeconds = 60, keyPrefix = "weather:search")
    @GetMapping("/search")
    public List<String> searchCity(
            @Parameter(description = "搜索关键词", example = "北")
            @RequestParam String keyword) {
        return weatherCollector.searchCity(keyword);
    }

    /**
     * 获取城市编码
     *
     * @param city 城市名称
     * @return 城市编码
     */
    @Operation(summary = "获取城市编码", description = "根据城市名称获取对应的城市编码")
    @RateLimited(permits = 60, windowSeconds = 60, keyPrefix = "weather:code")
    @GetMapping("/code")
    public String getCityCode(
            @Parameter(description = "城市名称", example = "北京")
            @RequestParam String city) {
        String code = weatherCollector.getCityCode(city);
        if (code == null) {
            throw new BizException(ResponseCode.BIZ_ERROR, "未找到城市: " + city);
        }
        return code;
    }
}
