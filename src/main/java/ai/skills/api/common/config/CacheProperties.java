package ai.skills.api.common.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 创建时间：2026/03/19
 * 功能：缓存配置属性类。
 * 作者：Devil
 */
@Data
@ConfigurationProperties(prefix = "skills-api.cache")
public class CacheProperties {

    /**
     * 缓存类型：local（默认，无需 Redis）或 redis
     */
    private CacheType type = CacheType.LOCAL;

    /**
     * 本地缓存配置
     */
    private LocalCacheConfig local = new LocalCacheConfig();

    @Getter
    public enum CacheType {
        /**
         * 本地内存缓存（默认，无需 Redis，仅适用于单机部署）
         */
        LOCAL,
        /**
         * Redis 缓存（需要 Redis 服务）
         */
        REDIS
    }

    @Getter
    @Setter
    public static class LocalCacheConfig {
        /**
         * 最大缓存条目数（默认 10000）
         */
        private int maxSize = 10000;

        /**
         * 过期清理检查间隔（默认 1 分钟）
         */
        private Duration cleanupInterval = Duration.ofMinutes(1);
    }
}
