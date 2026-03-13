package ai.skills.api.fortune.model;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：每日运势查询结果。
 * 作者：Devil
 *
 * @param date         公历日期 (yyyy-MM-dd)
 * @param lunarDate    农历日期
 * @param yearGanZhi   年干支
 * @param monthGanZhi  月干支
 * @param dayGanZhi    日干支
 * @param zodiac       生肖
 * @param constellation 星座
 * @param suitable     今日宜
 * @param avoid        今日忌
 * @param horoscope    星座运势 (如果传入星座参数)
 */
public record FortuneResult(
        String date,
        String lunarDate,
        String yearGanZhi,
        String monthGanZhi,
        String dayGanZhi,
        String zodiac,
        String constellation,
        List<String> suitable,
        List<String> avoid,
        Horoscope horoscope
) {
    public record Horoscope(
            String constellation,
            int overallLuck,
            int loveLuck,
            int careerLuck,
            int wealthLuck,
            String summary
    ) {}
}
