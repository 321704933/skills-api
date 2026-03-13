package ai.skills.api.ip.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建时间：2026/03/13
 * 功能：IP 地理位置查询结果。
 * 作者：Devil
 */
@Schema(name = "IP 地理位置查询结果", description = "IP 地理位置信息")
public record IpQueryResult(
        @Schema(description = "查询的 IP 地址", example = "113.92.157.29")
        String ip,

        @Schema(description = "国家", example = "中国")
        String country,

        @Schema(description = "省份", example = "广东省")
        String province,

        @Schema(description = "城市", example = "深圳市")
        String city,

        @Schema(description = "运营商", example = "电信")
        String isp
) {
}
