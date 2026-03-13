package ai.skills.api.fortune.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：今日黄历查询结果（基于真实农历数据）。
 * 作者：Devil
 */
@Schema(name = "今日黄历", description = "今日黄历详细信息")
public record AlmanacResult(
        @Schema(description = "公历日期", example = "2026-03-13")
        String date,

        @Schema(description = "农历日期", example = "丙午年二月十五")
        String lunarDate,

        @Schema(description = "年干支", example = "丙午")
        String yearGanZhi,

        @Schema(description = "月干支", example = "辛卯")
        String monthGanZhi,

        @Schema(description = "日干支", example = "癸巳")
        String dayGanZhi,

        @Schema(description = "生肖", example = "马")
        String zodiac,

        @Schema(description = "今日宜")
        List<String> suitable,

        @Schema(description = "今日忌")
        List<String> avoid,

        @Schema(description = "节日列表")
        List<String> festivals,

        @Schema(description = "节气", example = "惊蛰")
        String jieQi,

        @Schema(description = "星期", example = "星期五")
        String week,

        @Schema(description = "星宿", example = "房")
        String xiu,

        @Schema(description = "星宿吉凶", example = "吉")
        String xiuLuck,

        @Schema(description = "彭祖百忌天干", example = "癸不词讼")
        String pengZuGan,

        @Schema(description = "彭祖百忌地支", example = "巳不远行")
        String pengZuZhi,

        @Schema(description = "喜神方位", example = "东南")
        String positionXi,

        @Schema(description = "福神方位", example = "正东")
        String positionFu,

        @Schema(description = "财神方位", example = "正南")
        String positionCai,

        @Schema(description = "日冲", example = "蛇")
        String dayChong,

        @Schema(description = "日煞", example = "北")
        String daySha,

        @Schema(description = "年纳音", example = "天河水")
        String yearNaYin,

        @Schema(description = "月纳音", example = "石榴木")
        String monthNaYin,

        @Schema(description = "日纳音", example = "长流水")
        String dayNaYin,

        @Schema(description = "月相", example = "满月")
        String yueXiang
) {}
