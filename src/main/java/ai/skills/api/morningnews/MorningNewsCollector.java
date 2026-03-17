package ai.skills.api.morningnews;

/**
 * 创建时间：2026/03/17
 * 功能：早报采集器策略接口，每个分类提供独立实现。
 * <p>
 * 新增分类只需：
 * <ol>
 *     <li>实现此接口并注册为 Spring Bean（Bean 名称与 YAML 分类 key 一致）</li>
 *     <li>在 YAML 中添加对应分类配置并设置 enabled: true</li>
 * </ol>
 * 作者：Devil
 */
public interface MorningNewsCollector {

    /**
     * 功能：返回分类标识，需与 YAML 配置中的分类 key 一致。
     *
     * @return 分类标识（如 general、finance）
     */
    String category();

    /**
     * 功能：执行早报数据采集。
     *
     * @return 采集结果
     */
    MorningNewsResult collect();
}
