package ai.skills.api.morningnews.config;

import ai.skills.api.morningnews.MorningNewsCollector;
import ai.skills.api.morningnews.MorningNewsResult;
import ai.skills.api.morningnews.service.MorningNewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Map;

/**
 * 创建时间：2026/03/17
 * 功能：早报调度配置中心，启动时读取 YAML 配置，为每个已启用分类动态注册 CronTask。
 * <p>
 * 核心逻辑：
 * <ol>
 *     <li>遍历 {@code skills-api.morning-news.categories} 配置</li>
 *     <li>按 Bean 名称匹配 {@link MorningNewsCollector} 实现</li>
 *     <li>为匹配成功且已启用的分类注册定时采集任务</li>
 *     <li>采集结果通过 {@link MorningNewsService} 缓存至 Redis</li>
 *     <li>单个分类采集失败不影响其他分类</li>
 * </ol>
 * 作者：Devil
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties(MorningNewsProperties.class)
@ConditionalOnProperty(prefix = "skills-api.morning-news", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MorningNewsSchedulerConfig implements SchedulingConfigurer {

    private final MorningNewsProperties properties;
    private final Map<String, MorningNewsCollector> collectors;
    private final MorningNewsService morningNewsService;

    /**
     * 功能：构造注入调度配置、所有已注册的采集器和早报服务。
     *
     * @param properties         调度配置属性
     * @param collectors         Spring 容器中所有 MorningNewsCollector 实现（key 为 Bean 名称）
     * @param morningNewsService 早报数据服务
     */
    public MorningNewsSchedulerConfig(MorningNewsProperties properties,
                                      Map<String, MorningNewsCollector> collectors,
                                      MorningNewsService morningNewsService) {
        this.properties = properties;
        this.collectors = collectors;
        this.morningNewsService = morningNewsService;
    }

    /**
     * 功能：提供调度专用线程池，线程名前缀 {@code morningnews-}。
     *
     * @return 线程池调度器
     */
    @Bean
    public ThreadPoolTaskScheduler morningNewsTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getThreadPoolSize());
        scheduler.setThreadNamePrefix("morningnews-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(morningNewsTaskScheduler());

        properties.getCategories().forEach((category, config) -> {
            if (!config.isEnabled()) {
                log.info("早报分类 [{}] 已禁用，跳过调度注册", category);
                return;
            }

            MorningNewsCollector collector = collectors.get(category);
            if (collector == null) {
                log.warn("早报分类 [{}] 已启用但未找到对应的采集器 Bean，跳过调度注册", category);
                return;
            }

            taskRegistrar.addCronTask(new CronTask(
                    () -> executeCollect(collector),
                    config.getCron()
            ));
            log.info("注册早报分类 [{}] 调度任务，Cron 表达式：{}", category, config.getCron());
        });
    }

    /**
     * 功能：执行单个分类的早报采集，通过 Service 缓存至 Redis，单个分类异常不影响其他分类。
     *
     * @param collector 采集器实例
     */
    private void executeCollect(MorningNewsCollector collector) {
        String category = collector.category();
        try {
            log.info("开始采集早报分类 [{}] 数据", category);
            MorningNewsResult result = collector.collect();
            morningNewsService.cache(result);
        } catch (Exception e) {
            log.error("早报分类 [{}] 采集失败", category, e);
        }
    }
}
