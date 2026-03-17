package ai.skills.api.weather.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 天气预报响应数据（完整版）
 * 包含实时天气、空气质量、7天预报、逐小时预报、24小时观测历史和生活指数
 *
 * @author Devil
 * @since 2025-03-16
 */
@Schema(name = "天气预报响应", description = "天气预报响应数据")
public record WeatherResult(
        @Schema(description = "城市名称")
        String city,

        @Schema(description = "城市编码")
        String cityCode,

        @Schema(description = "数据更新时间")
        String updateTime,

        @Schema(description = "实时天气数据")
        CurrentWeather current,

        @Schema(description = "7天天气预报（包含逐小时预报和生活指数）")
        List<DailyWeather> forecast,

        @Schema(description = "过去24小时整点观测数据")
        List<Observation> observations
) {

    /**
     * 实时天气数据（来自气象站观测）
     */
    @Schema(name = "实时天气数据", description = "实时天气数据")
    public record CurrentWeather(
            @Schema(description = "当前温度", example = "7.8")
            String temp,

            @Schema(description = "天气现象", example = "晴")
            String weather,

            @Schema(description = "风向", example = "南风")
            String windDirection,

            @Schema(description = "风力", example = "2级")
            String windPower,

            @Schema(description = "相对湿度", example = "46%")
            String humidity,

            @Schema(description = "降水量（毫米）", example = "0")
            String rain,

            @Schema(description = "气压（百帕）", example = "1018")
            String pressure,

            @Schema(description = "数据更新时间", example = "15:43")
            String time
    ) {
    }


    /**
     * 每日天气数据
     */
    @Schema(name = "每日天气数据", description = "每日天气数据")
    public record DailyWeather(
            @Schema(description = "日期", example = "16日（今天）")
            String date,

            @Schema(description = "白天天气现象", example = "晴")
            String dayWeather,

            @Schema(description = "夜间天气现象", example = "多云")
            String nightWeather,

            @Schema(description = "最高温度", example = "15")
            String tempHigh,

            @Schema(description = "最低温度", example = "2")
            String tempLow,

            @Schema(description = "白天风向", example = "南风")
            String dayWindDirection,

            @Schema(description = "白天风力", example = "<3级")
            String dayWindPower,

            @Schema(description = "夜间风向", example = "北风")
            String nightWindDirection,

            @Schema(description = "夜间风力", example = "<3级")
            String nightWindPower,

            @Schema(description = "日出时间", example = "06:22")
            String sunrise,

            @Schema(description = "日落时间", example = "18:22")
            String sunset,

            @Schema(description = "逐小时预报")
            List<HourlyWeather> hourly,

            @Schema(description = "生活指数")
            List<LifeIndex> lifeIndices
    ) {
    }

    /**
     * 逐小时天气数据
     */
    @Schema(name = "逐小时天气数据", description = "逐小时天气数据")
    public record HourlyWeather(
            @Schema(description = "时间", example = "08:00")
            String time,

            @Schema(description = "天气现象", example = "多云")
            String weather,

            @Schema(description = "温度", example = "7")
            String temp,

            @Schema(description = "风向", example = "南风")
            String windDirection,

            @Schema(description = "风力", example = "<3级")
            String windPower
    ) {
    }

    /**
     * 生活指数
     */
    @Schema(name = "生活指数", description = "生活指数")
    public record LifeIndex(
            @Schema(description = "指数名称", example = "穿衣指数")
            String name,

            @Schema(description = "指数等级", example = "较冷")
            String level,

            @Schema(description = "指数说明", example = "建议着厚外套加毛衣等服装")
            String description
    ) {
    }

    /**
     * 过去24小时整点观测数据
     */
    @Schema(name = "整点观测数据", description = "过去24小时整点观测数据")
    public record Observation(
            @Schema(description = "观测时间（整点）", example = "15")
            String hour,

            @Schema(description = "温度", example = "7.8")
            String temp,

            @Schema(description = "风向", example = "南风")
            String windDirection,

            @Schema(description = "风力", example = "2")
            String windPower,

            @Schema(description = "相对湿度", example = "46")
            String humidity
    ) {
    }
}
