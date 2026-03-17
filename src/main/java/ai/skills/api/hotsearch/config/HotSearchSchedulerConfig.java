package ai.skills.api.hotsearch.config;

import ai.skills.api.hotsearch.HotSearchCollector;
import ai.skills.api.hotsearch.HotSearchResult;
import ai.skills.api.hotsearch.service.HotSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Map;

/**
 * 创建时间：2026/03/13
 * 功能：热搜调度配置中心，启动时读取 YAML 配置，为每个已启用平台动态注册 CronTask。
 * <p>
 * 核心逻辑：
 * <ol>
 *     <li>遍历 {@code skills-api.scheduler.platforms} 配置</li>
 *     <li>按 Bean 名称匹配 {@link HotSearchCollector} 实现</li>
 *     <li>为匹配成功且已启用的平台注册定时采集任务</li>
 *     <li>采集结果通过 {@link HotSearchService} 缓存至 Redis</li>
 *     <li>单个平台采集失败不影响其他平台</li>
 * </ol>
 * 作者：Devil
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "skills-api.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HotSearchSchedulerConfig implements SchedulingConfigurer {

    private final SchedulerProperties properties;
    private final Map<String, HotSearchCollector> collectors;
    private final HotSearchService hotSearchService;

    /**
     * 功能：构造注入调度配置、所有已注册的采集器和热搜服务。
     *
     * @param properties     调度配置属性
     * @param collectors     Spring 容器中所有 HotSearchCollector 实现（key 为 Bean 名称）
     * @param hotSearchService 热搜数据服务
     */
    public HotSearchSchedulerConfig(SchedulerProperties properties,
                                    Map<String, HotSearchCollector> collectors,
                                    HotSearchService hotSearchService) {
        this.properties = properties;
        this.collectors = collectors;
        this.hotSearchService = hotSearchService;
    }

    /**
     * 功能：提供调度专用线程池，线程名前缀 {@code hotsearch-}。
     *
     * @return 线程池调度器
     */
    @Bean
    public ThreadPoolTaskScheduler hotSearchTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getThreadPoolSize());
        scheduler.setThreadNamePrefix("hotsearch-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(hotSearchTaskScheduler());

        properties.getPlatforms().forEach((platform, config) -> {
            if (!config.isEnabled()) {
                log.info("平台 [{}] 已禁用，跳过调度注册", platform);
                return;
            }

            HotSearchCollector collector = collectors.get(platform);
            if (collector == null) {
                log.warn("平台 [{}] 已启用但未找到对应的采集器 Bean，跳过调度注册", platform);
                return;
            }

            taskRegistrar.addCronTask(new CronTask(
                    () -> executeCollect(collector),
                    config.getCron()
            ));
            log.info("注册平台 [{}] 调度任务，Cron 表达式：{}", platform, config.getCron());
        });
    }

    /**
     * 功能：执行单个平台的热搜采集，通过 Service 缓存至 Redis，单个平台异常不影响其他平台。
     *
     * @param collector 采集器实例
     */
    private void executeCollect(HotSearchCollector collector) {
        String platform = collector.platform();
        try {
            log.info("开始采集平台 [{}] 热搜数据", platform);
            HotSearchResult result = collector.collect();
            hotSearchService.cache(result);
        } catch (Exception e) {
            log.error("平台 [{}] 热搜采集失败", platform, e);
        }
    }
}
