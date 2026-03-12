package ai.skills.api.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 创建时间：2026/03/12
 * 功能：承载幂等配置，包括请求头名称和 TTL（Time To Live，生存时间）。
 * 作者：Devil
 */
@ConfigurationProperties(prefix = "skills-api.idempotency")
public class IdempotencyProperties {

    private boolean enabled = true;
    private String headerName = "X-Idempotency-Key";
    private Duration ttl = Duration.ofMinutes(10);

    /**
     * 功能：获取幂等功能是否启用。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 功能：设置幂等功能是否启用。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 功能：获取幂等请求头名称。
     *
     * @return 请求头名称
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * 功能：设置幂等请求头名称。
     *
     * @param headerName 请求头名称
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * 功能：获取 TTL（Time To Live，生存时间）配置。
     *
     * @return 生存时间
     */
    public Duration getTtl() {
        return ttl;
    }

    /**
     * 功能：设置 TTL（Time To Live，生存时间）配置。
     *
     * @param ttl 生存时间
     */
    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
