package ai.skills.api.stockindex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建时间：2026/03/18
 * 功能：绑定 {@code skills-api.stock-index.*} 配置，包含模块总开关、缓存 TTL 和各分组指数代码配置。
 * 作者：Devil
 */
@ConfigurationProperties(prefix = "skills-api.stock-index")
public class StockIndexProperties {

    /**
     * 模块总开关，默认启用
     */
    private boolean enabled = true;

    /**
     * 缓存有效期，默认 5 分钟
     */
    private Duration cacheTtl = Duration.ofMinutes(5);

    /**
     * 各分组的股票代码配置
     * key: 分组标识（a-share, hk, us, global, commodity）
     * value: 该分组下的指数代码配置
     */
    private Map<String, IndexGroupConfig> groups = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public Map<String, IndexGroupConfig> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, IndexGroupConfig> groups) {
        this.groups = groups;
    }

    /**
     * 单个分组的配置
     */
    public static class IndexGroupConfig {

        /**
         * 分组中文名
         */
        private String name;

        /**
         * 分组是否启用
         */
        private boolean enabled = true;

        /**
         * 股票代码列表
         */
        private List<String> codes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getCodes() {
            return codes;
        }

        public void setCodes(List<String> codes) {
            this.codes = codes;
        }
    }
}
