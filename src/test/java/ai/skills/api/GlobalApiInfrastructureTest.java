package ai.skills.api;

import ai.skills.api.common.idempotency.IdempotencyStore;
import ai.skills.api.common.ratelimit.RateLimiter;
import ai.skills.api.common.redis.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 创建时间：2026/03/12
 * 功能：验证全局 API（应用程序编程接口）基础设施，包括统一响应、异常、幂等和限流。
 * 作者：Devil
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalApiInfrastructureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 功能：在每个测试执行前清理 Redis 中的测试数据。
     */
    @BeforeEach
    void setUp() {
        // 清理测试相关的 Redis 键
        redissonClient.getKeys().getKeysByPattern("idempotent:*").forEach(key ->
                redissonClient.getBucket(key).delete()
        );
        redissonClient.getKeys().getKeysByPattern("ratelimit:*").forEach(key ->
                redissonClient.getBucket(key).delete()
        );
    }

    /**
     * 功能：验证成功响应会被统一包装，并且返回 traceId（链路追踪编号）。
     */
    @Test
    void shouldWrapSuccessfulResponseAndExposeTraceId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/demo/ping"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("请求成功"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.message").value("服务正常"))
                .andReturn();

        String traceId = result.getResponse().getHeader("X-Trace-Id");
        String responseBody = result.getResponse().getContentAsString();
        assertThat(objectMapper.readTree(responseBody).path("traceId").asText()).isEqualTo(traceId);
    }

    /**
     * 功能：验证客户端主动传入的 traceId 会被服务端复用。
     */
    @Test
    void shouldReuseProvidedTraceId() throws Exception {
        String traceId = "trace-demo-001";

        mockMvc.perform(get("/api/v1/demo/ping").header("X-Trace-Id", traceId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", traceId))
                .andExpect(jsonPath("$.traceId").value(traceId));
    }

    /**
     * 功能：验证参数校验失败时会返回统一错误结构。
     */
    @Test
    void shouldReturnStandardValidationErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/demo/tasks")
                        .header("X-Idempotency-Key", "task-validation-001")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "source": "",
                                  "keyword": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请求参数校验失败"))
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.path").value("/api/v1/demo/tasks"))
                .andExpect(jsonPath("$.errors[0].field").exists())
                .andExpect(jsonPath("$.errors[*].message", hasItem("source 不能为空")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("keyword 不能为空")));
    }

    /**
     * 功能：验证业务异常会输出标准错误响应。
     */
    @Test
    void shouldReturnBizExceptionWithStandardBody() throws Exception {
        mockMvc.perform(get("/api/v1/demo/biz-error"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("演示业务异常"));
    }

    /**
     * 功能：验证开启幂等后，缺少幂等键的请求会被拒绝。
     */
    @Test
    void shouldRejectRequestWithoutIdempotencyKey() throws Exception {
        mockMvc.perform(post("/api/v1/demo/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "source": "weather",
                                  "keyword": "shanghai"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请求缺少幂等键"));
    }

    /**
     * 功能：验证相同幂等键的重复请求会被识别为冲突。
     */
    @Test
    void shouldRejectDuplicateIdempotentRequest() throws Exception {
        String payload = """
                {
                  "source": "weather",
                  "keyword": "beijing"
                }
                """;

        mockMvc.perform(post("/api/v1/demo/tasks")
                        .header("X-Idempotency-Key", "task-create-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("beijing"));

        mockMvc.perform(post("/api/v1/demo/tasks")
                        .header("X-Idempotency-Key", "task-create-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("请求重复提交"));
    }

    /**
     * 功能：验证请求执行失败时会释放幂等占位，避免错误状态锁死后续重试。
     */
    @Test
    void shouldReleaseIdempotencyKeyWhenRequestFails() throws Exception {
        String payload = """
                {
                  "source": "weather",
                  "keyword": "fail"
                }
                """;

        mockMvc.perform(post("/api/v1/demo/tasks")
                        .header("X-Idempotency-Key", "task-fail-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("关键字 fail 为保留字"));

        mockMvc.perform(post("/api/v1/demo/tasks")
                        .header("X-Idempotency-Key", "task-fail-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("关键字 fail 为保留字"));
    }

    /**
     * 功能：验证同一客户端在窗口期内超过阈值后会被限流。
     */
    @Test
    void shouldRateLimitByClientIdentifier() throws Exception {
        // 使用唯一 IP 避免与之前测试运行的残留数据冲突
        String clientIp = "203.0.113." + System.currentTimeMillis() % 256;

        mockMvc.perform(get("/api/v1/demo/hot-search").header("X-Forwarded-For", clientIp))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Remaining", "1"));

        mockMvc.perform(get("/api/v1/demo/hot-search").header("X-Forwarded-For", clientIp))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "0"));

        mockMvc.perform(get("/api/v1/demo/hot-search").header("X-Forwarded-For", clientIp))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").value("请求过于频繁"));
    }
}
