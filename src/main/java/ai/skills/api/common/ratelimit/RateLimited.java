package ai.skills.api.common.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建时间：2026/03/12
 * 功能：声明接口需要启用限流控制。
 * 作者：Devil
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * 功能：定义窗口内允许的请求次数。
     *
     * @return 请求次数，小于等于 0 表示使用全局配置
     */
    int permits() default -1;

    /**
     * 功能：定义窗口时长，单位为秒。
     *
     * @return 窗口秒数，小于等于 0 表示使用全局配置
     */
    long windowSeconds() default -1;

    /**
     * 功能：自定义限流键前缀。
     *
     * @return 键前缀，为空则使用默认规则
     */
    String keyPrefix() default "";

    /**
     * 功能：限流策略。
     *
     * @return 限流策略类型
     */
    Strategy strategy() default Strategy.SLIDING_WINDOW;

    /**
     * 创建时间：2026/03/12
     * 功能：限流策略枚举。
     * 作者：Devil
     */
    enum Strategy {
        /** 滑动窗口算法 - 平滑限流 */
        SLIDING_WINDOW,
        /** 固定窗口算法 - 简单高效 */
        FIXED_WINDOW,
        /** 令牌桶算法 - 支持突发流量 */
        TOKEN_BUCKET
    }
}
