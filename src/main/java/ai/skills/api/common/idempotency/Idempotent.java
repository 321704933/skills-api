package ai.skills.api.common.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建时间：2026/03/12
 * 功能：声明接口需要开启幂等保护。
 * 作者：Devil
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 功能：定义幂等记录的 TTL（Time To Live，生存时间），单位为秒。
     *
     * @return 生存时间秒数，小于等于 0 表示使用全局配置
     */
    long ttlSeconds() default -1;

    /**
     * 功能：指定自定义幂等请求头名称。
     *
     * @return 请求头名称，留空表示使用全局配置
     */
    String headerName() default "";

    /**
     * 功能：自定义幂等键前缀。
     *
     * @return 键前缀，为空则使用默认规则
     */
    String keyPrefix() default "";

    /**
     * 功能：是否在请求失败时释放幂等键（允许重试）。
     *
     * @return 是否释放
     */
    boolean releaseOnError() default true;

    /**
     * 功能：幂等键来源。
     *
     * @return 键来源类型
     */
    KeySource keySource() default KeySource.HEADER;

    /**
     * 创建时间：2026/03/12
     * 功能：幂等键来源枚举。
     * 作者：Devil
     */
    enum KeySource {
        /** 从请求头获取 */
        HEADER,
        /** 从请求参数获取 */
        PARAM,
        /** 从请求体 JSON 字段获取 */
        BODY,
        /** 使用 SpEL 表达式计算 */
        EXPRESSION
    }
}
