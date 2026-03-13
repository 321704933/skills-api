package ai.skills.api.common;

import ai.skills.api.common.config.IdempotencyProperties;
import ai.skills.api.common.config.RateLimitProperties;
import ai.skills.api.common.config.WebProperties;
import ai.skills.api.hotsearch.config.SchedulerProperties;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;

/**
 * 创建时间：2026/03/12
 * 功能：Skills API 脚手架自动配置入口。
 * 作者：Devil
 */
@AutoConfiguration
@EnableConfigurationProperties({
        WebProperties.class,
        RateLimitProperties.class,
        IdempotencyProperties.class,
        SchedulerProperties.class
})
@Import({
        ai.skills.api.common.config.RedisStorageConfig.class,
        ai.skills.api.common.config.WebMvcConfig.class
})
public class SkillsApiAutoConfiguration {

    /**
     * 功能：提供默认时钟实例。
     *
     * @return 系统默认时钟
     */
    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
