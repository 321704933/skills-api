package ai.skills.api.fortune.service;

import ai.skills.api.fortune.model.FortuneResult;
import ai.skills.api.fortune.model.FortuneResult.Horoscope;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * 创建时间：2026/03/13
 * 功能：每日运势服务，提供农历、宜忌、星座运势等信息。
 * 作者：Devil
 */
@Service
public class FortuneService {

    // 天干
    private static final String[] TIANGAN = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    // 地支
    private static final String[] DIZHI = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    // 生肖
    private static final String[] ZODIAC = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    // 农历月份
    private static final String[] LUNAR_MONTH = {"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
    // 农历日期
    private static final String[] LUNAR_DAY = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };
    // 星座名称
    private static final String[] CONSTELLATIONS = {
            "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座",
            "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"
    };
    // 宜事项
    private static final List<String> SUITABLE_ITEMS = List.of(
            "写代码", "修Bug", "提交代码", "重构", "Code Review", "摸鱼", "喝咖啡",
            "开会", "写文档", "学习新技术", "看技术博客", "逛GitHub", "写单元测试",
            "部署上线", "备份数据", "优化性能", "架构设计", "需求评审", "画流程图"
    );
    // 忌事项
    private static final List<String> AVOID_ITEMS = List.of(
            "删库跑路", "随意上线", "改配置", "动数据库", "改接口", "不写测试",
            "过度设计", "复制粘贴", "忽略警告", "跳过Review", "加班", "熬夜",
            "饮酒编码", "情绪化提交", "随意合并", "忽略异常", "硬编码"
    );
    // 运势文案
    private static final List<String> HOROSCOPE_SUMMARIES = List.of(
            "今日适合专注核心任务，避免分心。代码如有神助，Bug 绕道而行。",
            "运势平稳，适合处理日常事务。注意代码规范，避免技术债务累积。",
            "创意迸发的一天，适合尝试新技术。但切记先写测试，再重构。",
            "宜静不宜动，适合代码审查和文档整理。重要决策建议推迟。",
            "今日桃花运旺，但代码中不要有耦合。保持模块独立，爱情也会顺利。",
            "财运亨通，但不要用金钱解决技术问题。投资学习才是正道。",
            "事业运上升，领导会注意到你的贡献。记得提交前自测，避免低级错误。",
            "今日宜低调，避免在群里争论技术选型。默默优化，让代码说话。",
            "贵人相助，遇到问题多请教同事。但也要独立思考，不能总依赖他人。",
            "灵感爆发，适合攻克难题。但注意劳逸结合，代码写得好也要身体好。"
    );

    /**
     * 功能：获取指定日期的运势信息。
     *
     * @param date         日期 (yyyy-MM-dd)，为空则取今天
     * @param constellation 星座名称，为空则根据日期计算
     * @return 运势信息
     */
    public FortuneResult getFortune(String date, String constellation) {
        LocalDate localDate = (date != null && !date.isBlank())
                ? LocalDate.parse(date)
                : LocalDate.now();

        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        // 基于日期的随机种子，确保同一天结果一致
        long seed = year * 10000L + month * 100 + day;
        Random random = new Random(seed);

        // 计算干支
        String yearGanZhi = getYearGanZhi(year);
        String monthGanZhi = getMonthGanZhi(year, month);
        String dayGanZhi = getDayGanZhi(year, month, day);

        // 计算生肖
        String zodiac = getZodiac(year);

        // 计算星座
        String targetConstellation = (constellation != null && !constellation.isBlank())
                ? constellation
                : getConstellation(month, day);

        // 计算农历（简化版，基于春节偏移近似计算）
        String lunarDate = getLunarDate(year, month, day);

        // 随机选择宜忌
        List<String> suitable = selectRandom(SUITABLE_ITEMS, 3 + random.nextInt(3), random);
        List<String> avoid = selectRandom(AVOID_ITEMS, 2 + random.nextInt(2), random);

        // 计算星座运势
        Horoscope horoscope = getHoroscope(targetConstellation, random);

        return new FortuneResult(
                localDate.toString(),
                lunarDate,
                yearGanZhi,
                monthGanZhi,
                dayGanZhi,
                zodiac,
                getConstellation(month, day),
                suitable,
                avoid,
                horoscope
        );
    }

    private String getYearGanZhi(int year) {
        int ganIndex = (year - 4) % 10;
        int zhiIndex = (year - 4) % 12;
        return TIANGAN[ganIndex] + DIZHI[zhiIndex];
    }

    private String getMonthGanZhi(int year, int month) {
        // 简化计算：年干决定月干起点
        int yearGan = (year - 4) % 10;
        int ganIndex = (yearGan % 5 * 2 + month - 1) % 10;
        int zhiIndex = (month + 1) % 12;
        return TIANGAN[ganIndex] + DIZHI[zhiIndex];
    }

    private String getDayGanZhi(int year, int month, int day) {
        // 简化计算：基于日期偏移
        long baseDate = LocalDate.of(1900, 1, 31).toEpochDay();
        long targetDate = LocalDate.of(year, month, day).toEpochDay();
        long diff = targetDate - baseDate;
        int ganIndex = (int) ((diff % 10) + 10) % 10;
        int zhiIndex = (int) ((diff % 12) + 12) % 12;
        return TIANGAN[ganIndex] + DIZHI[zhiIndex];
    }

    private String getZodiac(int year) {
        int index = (year - 4) % 12;
        return ZODIAC[index >= 0 ? index : index + 12];
    }

    private String getConstellation(int month, int day) {
        // 星座日期分界
        int[] boundaries = {20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};
        int index = month - 1;
        if (day < boundaries[index]) {
            index = (index + 11) % 12;
        }
        return CONSTELLATIONS[index];
    }

    private String getLunarDate(int year, int month, int day) {
        // 简化版农历计算（实际应使用精确的农历库）
        // 这里基于春节日期近似计算
        int springFestivalMonth = 2; // 春节通常在1-2月
        int springFestivalDay = getSpringFestivalDay(year);

        LocalDate springFestival = LocalDate.of(year, springFestivalMonth == 1 ? 1 : 2, springFestivalDay);
        LocalDate target = LocalDate.of(year, month, day);

        long diff = target.toEpochDay() - springFestival.toEpochDay();

        // 简化：假设农历每月30天
        int lunarMonth = (int) (diff / 30) + 1;
        int lunarDay = (int) ((diff % 30) + 30) % 30;

        // 边界处理
        if (lunarMonth < 1) {
            lunarMonth = 12;
        } else if (lunarMonth > 12) {
            lunarMonth = lunarMonth % 12;
            if (lunarMonth == 0) lunarMonth = 12;
        }

        if (lunarDay < 0) lunarDay = 0;
        if (lunarDay >= 30) lunarDay = 29;

        String monthName = LUNAR_MONTH[(lunarMonth - 1 + 12) % 12] + "月";
        String dayName = LUNAR_DAY[lunarDay];

        return year + "年" + monthName + dayName;
    }

    private int getSpringFestivalDay(int year) {
        // 简化：春节通常在1月21日-2月20日之间
        // 实际应查表或使用精确算法
        return switch (year % 10) {
            case 0, 5 -> 26;
            case 1, 6 -> 12;
            case 2, 7 -> 1;
            case 3, 8 -> 10;
            default -> 19;
        };
    }

    private <T> List<T> selectRandom(List<T> source, int count, Random random) {
        return random.ints(0, source.size())
                .distinct()
                .limit(count)
                .mapToObj(source::get)
                .toList();
    }

    private Horoscope getHoroscope(String constellation, Random random) {
        int overall = 60 + random.nextInt(40);
        int love = 50 + random.nextInt(50);
        int career = 50 + random.nextInt(50);
        int wealth = 50 + random.nextInt(50);
        String summary = HOROSCOPE_SUMMARIES.get(random.nextInt(HOROSCOPE_SUMMARIES.size()));

        return new Horoscope(constellation, overall, love, career, wealth, summary);
    }
}
