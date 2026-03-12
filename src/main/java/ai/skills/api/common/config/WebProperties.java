package ai.skills.api.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 创建时间：2026/03/12
 * 功能：承载 Web 层通用配置，例如 traceId（链路追踪编号）响应头和响应包装开关。
 * 作者：Devil
 */
@ConfigurationProperties(prefix = "skills-api.web")
public class WebProperties {

    private String traceHeaderName = "X-Trace-Id";
    private boolean wrapSuccessResponse = true;

    /**
     * 功能：获取 traceId（链路追踪编号）请求头名称。
     *
     * @return 请求头名称
     */
    public String getTraceHeaderName() {
        return traceHeaderName;
    }

    /**
     * 功能：设置 traceId（链路追踪编号）请求头名称。
     *
     * @param traceHeaderName 请求头名称
     */
    public void setTraceHeaderName(String traceHeaderName) {
        this.traceHeaderName = traceHeaderName;
    }

    /**
     * 功能：获取是否启用成功响应统一包装。
     *
     * @return 是否启用
     */
    public boolean isWrapSuccessResponse() {
        return wrapSuccessResponse;
    }

    /**
     * 功能：设置是否启用成功响应统一包装。
     *
     * @param wrapSuccessResponse 是否启用
     */
    public void setWrapSuccessResponse(boolean wrapSuccessResponse) {
        this.wrapSuccessResponse = wrapSuccessResponse;
    }
}
