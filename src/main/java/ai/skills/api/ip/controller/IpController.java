package ai.skills.api.ip.controller;

import ai.skills.api.ip.model.IpQueryResult;
import ai.skills.api.ip.service.IpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/13
 * 功能：IP 地理位置查询接口。
 * 作者：Devil
 */
@Tag(name = "IP 查询")
@RestController
@RequestMapping("/api/v1/ip")
public class IpController {

    private final IpService ipService;

    public IpController(IpService ipService) {
        this.ipService = ipService;
    }

    /**
     * 功能：查询 IP 地址的地理位置信息。
     * 若未传入 ip 参数，则自动获取调用者 IP。
     *
     * @param ip      可选，待查询的 IPv4 地址
     * @param request HTTP 请求（用于获取调用者 IP）
     * @return IP 地理位置查询结果
     */
    @Operation(summary = "查询 IP 地理位置", description = "根据IP地址查询地理位置信息，不传IP则自动获取调用者IP")
    @GetMapping("/query")
    public IpQueryResult query(@RequestParam(required = false) String ip,
                               HttpServletRequest request) {
        if (ip == null || ip.isBlank()) {
            ip = getClientIp(request);
        }
        return ipService.query(ip);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // 取第一个（最原始的客户端 IP）
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
