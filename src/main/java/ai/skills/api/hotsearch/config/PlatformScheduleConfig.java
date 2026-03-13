package ai.skills.api.hotsearch.config;

/**
 * 创建时间：2026/03/13
 * 功能：承载单个热搜平台的调度配置，包括启停开关和 Cron 表达式。
 * 作者：Devil
 */
public class PlatformScheduleConfig {

    private boolean enabled = false;
    private String cron;

    /**
     * 功能：获取该平台调度是否启用。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 功能：设置该平台调度是否启用。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 功能：获取该平台的 Cron 表达式。
     *
     * @return Cron 表达式
     */
    public String getCron() {
        return cron;
    }

    /**
     * 功能：设置该平台的 Cron 表达式。
     *
     * @param cron Cron 表达式
     */
    public void setCron(String cron) {
        this.cron = cron;
    }
}
