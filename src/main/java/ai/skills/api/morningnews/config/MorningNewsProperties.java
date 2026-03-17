package ai.skills.api.morningnews.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 创建时间：2026/03/17
 * 功能：绑定 {@code skills-api.morning-news.*} 配置，包含总开关、线程池大小和各分类调度配置。
 * 作者：Devil
 */
@ConfigurationProperties(prefix = "skills-api.morning-news")
public class MorningNewsProperties {

    private boolean enabled = true;
    private int threadPoolSize = 2;
    private Map<String, CategoryScheduleConfig> categories = new LinkedHashMap<>();

    /**
     * 功能：获取早报调度系统总开关。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 功能：设置早报调度系统总开关。
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
     * 功能：获取各分类调度配置。
     *
     * @return 分类名称 → 调度配置
     */
    public Map<String, CategoryScheduleConfig> getCategories() {
        return categories;
    }

    /**
     * 功能：设置各分类调度配置。
     *
     * @param categories 分类名称 → 调度配置
     */
    public void setCategories(Map<String, CategoryScheduleConfig> categories) {
        this.categories = categories;
    }
}
