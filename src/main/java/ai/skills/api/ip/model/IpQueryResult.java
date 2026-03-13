package ai.skills.api.ip.model;

/**
 * 创建时间：2026/03/13
 * 功能：IP 地理位置查询结果。
 * 作者：Devil
 *
 * @param ip       查询的 IP 地址
 * @param country  国家
 * @param province 省份
 * @param city     城市
 * @param isp      运营商
 */
public record IpQueryResult(
        String ip,
        String country,
        String province,
        String city,
        String isp
) {
}
