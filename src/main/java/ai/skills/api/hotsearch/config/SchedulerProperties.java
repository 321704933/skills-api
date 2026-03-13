package ai.skills.api.hotsearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 创建时间：2026/03/13
 * 功能：绑定 {@code skills-api.scheduler.*} 配置，包含总开关、线程池大小和各平台调度配置。
 * 作者：Devil
 */
@ConfigurationProperties(prefix = "skills-api.scheduler")
public class SchedulerProperties {

    private boolean enabled = true;
    private int threadPoolSize = 4;
    private Map<String, PlatformScheduleConfig> platforms = new LinkedHashMap<>();

    /**
     * 功能：获取调度系统总开关。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 功能：设置调度系统总开关。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 功能：获取调度线程池大小。
     *
     * @return 线程池大小
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * 功能：设置调度线程池大小。
     *
     * @param threadPoolSize 线程池大小
     */
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * 功能：获取各平台调度配置。
     *
     * @return 平台名称 → 调度配置
     */
    public Map<String, PlatformScheduleConfig> getPlatforms() {
        return platforms;
    }

    /**
     * 功能：设置各平台调度配置。
     *
     * @param platforms 平台名称 → 调度配置
     */
    public void setPlatforms(Map<String, PlatformScheduleConfig> platforms) {
        this.platforms = platforms;
    }
}
