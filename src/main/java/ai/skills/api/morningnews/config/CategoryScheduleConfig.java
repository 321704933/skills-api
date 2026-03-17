package ai.skills.api.morningnews.config;

/**
 * 创建时间：2026/03/17
 * 功能：承载单个早报分类的调度配置，包括启停开关和 Cron 表达式。
 * 作者：Devil
 */
public class CategoryScheduleConfig {

    private boolean enabled = false;
    private String cron;

    /**
     * 功能：获取该分类调度是否启用。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 功能：设置该分类调度是否启用。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 功能：获取该分类的 Cron 表达式。
     *
     * @return Cron 表达式
     */
    public String getCron() {
        return cron;
    }

    /**
     * 功能：设置该分类的 Cron 表达式。
     *
     * @param cron Cron 表达式
     */
    public void setCron(String cron) {
        this.cron = cron;
    }
}
