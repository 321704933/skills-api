package ai.skills.api.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建时间：2026/03/19
 * 功能：根据缓存类型动态排除 Redisson 自动配置。
 * <p>
 * 当 skills-api.cache.type=local（默认）时，自动排除 RedissonAutoConfigurationV2，
 * 避免在没有 Redis 环境时启动失败。
 * </p>
 * 作者：Devil
 */
@Slf4j
public class CacheTypeEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String CACHE_TYPE_PROPERTY = "skills-api.cache.type";
    private static final String DEFAULT_CACHE_TYPE = "local";
    private static final String REDIS_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";
    private static final String REDISSON_AUTO_CONFIG_CLASS = "org.redisson.spring.starter.RedissonAutoConfigurationV2";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String cacheType = environment.getProperty(CACHE_TYPE_PROPERTY, DEFAULT_CACHE_TYPE);
        log.info("检测到缓存类型配置：{}", cacheType);

        if ("local".equalsIgnoreCase(cacheType)) {
            // 本地缓存模式，排除 Redisson 自动配置
            excludeRedissonAutoConfiguration(environment);
            log.info("本地缓存模式：已自动排除 Redisson 自动配置");
        } else if ("redis".equalsIgnoreCase(cacheType)) {
            log.info("Redis 缓存模式：Redisson 自动配置已启用");
        }
    }

    /**
     * 功能：将 RedissonAutoConfigurationV2 添加到排除列表
     *
     * @param environment Spring 环境
     */
    @SuppressWarnings("unchecked")
    private void excludeRedissonAutoConfiguration(ConfigurableEnvironment environment) {
        Map<String, Object> excludes = new HashMap<>();

        // 获取现有的排除配置
        Object existingExcludes = environment.getProperty(REDIS_AUTOCONFIGURE_EXCLUDE);
        List<String> excludeList;

        if (existingExcludes instanceof List) {
            excludeList = (List<String>) existingExcludes;
        } else if (existingExcludes instanceof String) {
            excludeList = List.of(((String) existingExcludes).split(","));
        } else {
            excludeList = List.of();
        }

        // 如果已经排除了 Redisson，则无需重复添加
        if (excludeList.contains(REDISSON_AUTO_CONFIG_CLASS)) {
            return;
        }

        // 创建新的排除列表
        excludes.put(REDIS_AUTOCONFIGURE_EXCLUDE, List.of(REDISSON_AUTO_CONFIG_CLASS));

        // 添加到环境配置中
        MapPropertySource propertySource = new MapPropertySource(
                "cacheTypeAutoConfigurationExcludes",
                excludes
        );
        environment.getPropertySources().addFirst(propertySource);
    }
}
