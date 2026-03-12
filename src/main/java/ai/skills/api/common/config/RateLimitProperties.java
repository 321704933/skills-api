package ai.skills.api.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 创建时间：2026/03/12
 * 功能：承载限流配置，包括客户端标识请求头、默认窗口和默认配额。
 * 作者：Devil
 */
@ConfigurationProperties(prefix = "skills-api.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private String clientIdentifierHeader = "X-Forwarded-For";
    private Duration defaultWindow = Duration.ofMinutes(1);
    private int defaultPermits = 60;

    /**
     * 功能：获取限流功能是否启用。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 功能：设置限流功能是否启用。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 功能：获取客户端标识请求头名称。
     *
     * @return 请求头名称
     */
    public String getClientIdentifierHeader() {
        return clientIdentifierHeader;
    }

    /**
     * 功能：设置客户端标识请求头名称。
     *
     * @param clientIdentifierHeader 请求头名称
     */
    public void setClientIdentifierHeader(String clientIdentifierHeader) {
        this.clientIdentifierHeader = clientIdentifierHeader;
    }

    /**
     * 功能：获取默认限流窗口时长。
     *
     * @return 窗口时长
     */
    public Duration getDefaultWindow() {
        return defaultWindow;
    }

    /**
     * 功能：设置默认限流窗口时长。
     *
     * @param defaultWindow 窗口时长
     */
    public void setDefaultWindow(Duration defaultWindow) {
        this.defaultWindow = defaultWindow;
    }

    /**
     * 功能：获取默认允许请求次数。
     *
     * @return 允许请求次数
     */
    public int getDefaultPermits() {
        return defaultPermits;
    }

    /**
     * 功能：设置默认允许请求次数。
     *
     * @param defaultPermits 允许请求次数
     */
    public void setDefaultPermits(int defaultPermits) {
        this.defaultPermits = defaultPermits;
    }
}
