package ai.skills.api.hotsearch;

/**
 * 创建时间：2026/03/13
 * 功能：热搜采集器策略接口，每个平台提供独立实现。
 * <p>
 * 新增平台只需：
 * <ol>
 *     <li>实现此接口并注册为 Spring Bean（Bean 名称与 YAML 平台 key 一致）</li>
 *     <li>在 YAML 中添加对应平台配置并设置 enabled: true</li>
 * </ol>
 * 作者：Devil
 */
public interface HotSearchCollector {

    /**
     * 功能：返回平台标识，需与 YAML 配置中的平台 key 一致。
     *
     * @return 平台标识（如 baidu、weibo）
     */
    String platform();

    /**
     * 功能：执行热搜数据采集。
     *
     * @return 采集结果
     */
    HotSearchResult collect();
}
