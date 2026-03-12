package ai.skills.api.common.idempotency;

import java.time.Duration;
import java.time.Instant;

/**
 * 创建时间：2026/03/12
 * 功能：定义幂等存储抽象，便于后续从内存切换到 Redis（远程字典服务）等实现。
 * 作者：Devil
 */
public interface IdempotencyStore {

    /**
     * 功能：尝试占用一个幂等键。
     *
     * @param key 幂等键
     * @param ttl TTL（Time To Live，生存时间）
     * @param now 当前时间
     * @return 是否占用成功
     */
    boolean tryStart(String key, Duration ttl, Instant now);

    /**
     * 功能：把幂等键标记为已完成状态。
     *
     * @param key 幂等键
     * @param ttl TTL（Time To Live，生存时间）
     * @param now 当前时间
     */
    void markCompleted(String key, Duration ttl, Instant now);

    /**
     * 功能：释放幂等键占位。
     *
     * @param key 幂等键
     */
    void release(String key);
}
