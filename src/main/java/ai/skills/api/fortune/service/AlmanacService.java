package ai.skills.api.fortune.service;

import ai.skills.api.fortune.model.AlmanacResult;
import com.nlf.calendar.Lunar;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：今日黄历服务，提供基于真实老黄历的农历数据。
 * 作者：Devil
 */
@Service
public class AlmanacService {

    /**
     * 功能：获取今日黄历（基于真实老黄历数据）
     *
     * @return 黄历信息
     */
    public AlmanacResult getAlmanac() {
        // 使用农历库获取精确数据
        Lunar lunar = Lunar.fromDate(new Date());

        // 获取干支
        String yearGanZhi = lunar.getYearInGanZhi();
        String monthGanZhi = lunar.getMonthInGanZhi();
        String dayGanZhi = lunar.getDayInGanZhi();

        // 获取生肖
        String zodiac = lunar.getYearShengXiao();

        // 获取农历日期（格式：丙午年二月十五）
        String lunarDate = lunar.getYearInGanZhi() + "年" + lunar.getMonthInChinese() + "月" + lunar.getDayInChinese();

        // 获取真实的宜忌数据（基于老黄历）
        List<String> suitable = lunar.getDayYi();
        List<String> avoid = lunar.getDayJi();

        // 获取节日信息
        List<String> festivals = new ArrayList<>();
        festivals.addAll(lunar.getFestivals());
        festivals.addAll(lunar.getOtherFestivals());

        // 获取节气
        String jieQi = lunar.getJieQi();

        // 获取星期
        String week = lunar.getWeekInChinese();

        // 获取星宿信息
        String xiu = lunar.getXiu();
        String xiuLuck = lunar.getXiuLuck();

        // 获取彭祖百忌
        String pengZuGan = lunar.getPengZuGan();
        String pengZuZhi = lunar.getPengZuZhi();

        // 获取神位方位
        String positionXi = lunar.getDayPositionXiDesc();
        String positionFu = lunar.getDayPositionFuDesc();
        String positionCai = lunar.getDayPositionCaiDesc();

        // 获取冲煞
        String dayChong = lunar.getDayChongDesc();
        String daySha = lunar.getDaySha();

        // 获取纳音
        String yearNaYin = lunar.getYearNaYin();
        String monthNaYin = lunar.getMonthNaYin();
        String dayNaYin = lunar.getDayNaYin();

        // 获取月相
        String yueXiang = lunar.getYueXiang();

        return new AlmanacResult(
                lunar.getSolar().toYmd(),
                lunarDate,
                yearGanZhi,
                monthGanZhi,
                dayGanZhi,
                zodiac,
                suitable,
                avoid,
                festivals,
                jieQi,
                week,
                xiu,
                xiuLuck,
                pengZuGan,
                pengZuZhi,
                positionXi,
                positionFu,
                positionCai,
                dayChong,
                daySha,
                yearNaYin,
                monthNaYin,
                dayNaYin,
                yueXiang
        );
    }
}
