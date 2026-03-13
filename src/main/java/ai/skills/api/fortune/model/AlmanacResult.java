package ai.skills.api.fortune.model;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：今日黄历查询结果（基于真实农历数据）。
 * 作者：Devil
 *
 * @param date            公历日期 (yyyy-MM-dd)
 * @param lunarDate       农历日期（中文，如：丙午年二月十五）
 * @param yearGanZhi      年干支
 * @param monthGanZhi     月干支
 * @param dayGanZhi       日干支
 * @param zodiac          生肖
 * @param suitable        今日宜（基于老黄历）
 * @param avoid           今日忌（基于老黄历）
 * @param festivals       节日列表
 * @param jieQi           节气（如果有）
 * @param week            星期
 * @param xiu             星宿
 * @param xiuLuck         星宿吉凶
 * @param pengZuGan       彭祖百忌天干
 * @param pengZuZhi       彭祖百忌地支
 * @param positionXi      喜神方位
 * @param positionFu      福神方位
 * @param positionCai     财神方位
 * @param dayChong        日冲
 * @param daySha          日煞
 * @param yearNaYin       年纳音
 * @param monthNaYin      月纳音
 * @param dayNaYin        日纳音
 * @param yueXiang        月相
 */
public record AlmanacResult(
        String date,
        String lunarDate,
        String yearGanZhi,
        String monthGanZhi,
        String dayGanZhi,
        String zodiac,
        List<String> suitable,
        List<String> avoid,
        List<String> festivals,
        String jieQi,
        String week,
        String xiu,
        String xiuLuck,
        String pengZuGan,
        String pengZuZhi,
        String positionXi,
        String positionFu,
        String positionCai,
        String dayChong,
        String daySha,
        String yearNaYin,
        String monthNaYin,
        String dayNaYin,
        String yueXiang
) {}
