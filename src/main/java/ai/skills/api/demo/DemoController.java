package ai.skills.api.demo;

import ai.skills.api.common.exception.BizException;
import ai.skills.api.common.api.ResponseCode;
import ai.skills.api.common.idempotency.Idempotent;
import ai.skills.api.common.ratelimit.RateLimited;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2026/03/12
 * 功能：提供基础演示接口，展示统一响应、幂等、限流和异常处理的接入方式。
 * 作者：Devil
 */
@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    private static final AtomicLong TASK_SEQUENCE = new AtomicLong(1000);

    /**
     * 功能：提供基础存活探针接口。
     *
     * @return 探针响应
     */
    @GetMapping("/ping")
    public PingView ping() {
        return new PingView("服务正常", Instant.now().toString());
    }

    /**
     * 功能：演示业务异常输出效果。
     *
     * @return 理论上不会返回成功数据
     */
    @GetMapping("/biz-error")
    public PingView bizError() {
        throw new BizException(ResponseCode.BIZ_ERROR, "演示业务异常");
    }

    /**
     * 功能：演示带幂等控制的任务创建接口。
     *
     * @param request 创建任务请求体
     * @return 创建结果
     */
    @PostMapping("/tasks")
    @Idempotent
    public CreatedTaskView createTask(@Valid @RequestBody CreateTaskRequest request) {
        if ("fail".equalsIgnoreCase(request.keyword())) {
            throw new BizException(ResponseCode.BIZ_ERROR, "关键字 fail 为保留字");
        }

        return new CreatedTaskView(
                "task-" + TASK_SEQUENCE.incrementAndGet(),
                request.source(),
                request.keyword(),
                Instant.now().toString()
        );
    }

    /**
     * 功能：演示带限流控制的热搜查询接口。
     *
     * @return 热搜列表
     */
    @GetMapping("/hot-search")
    @RateLimited(permits = 2, windowSeconds = 60)
    public HotSearchView hotSearch() {
        return new HotSearchView(List.of(
                new HotKeyword("weather", 9800),
                new HotKeyword("baidu-hot", 9400),
                new HotKeyword("douyin-hot", 9100)
        ));
    }

    /**
     * 创建时间：2026/03/12
     * 功能：定义探针接口返回结构。
     * 作者：Devil
     *
     * @param message 响应消息
     * @param now 当前时间
     */
    public record PingView(String message, String now) {
    }

    /**
     * 创建时间：2026/03/12
     * 功能：定义任务创建成功后的返回结构。
     * 作者：Devil
     *
     * @param taskId taskId（任务编号）
     * @param source 数据来源
     * @param keyword 任务关键词
     * @param createdAt 创建时间
     */
    public record CreatedTaskView(String taskId, String source, String keyword, String createdAt) {
    }

    /**
     * 创建时间：2026/03/12
     * 功能：定义热搜列表返回结构。
     * 作者：Devil
     *
     * @param items 热搜条目列表
     */
    public record HotSearchView(List<HotKeyword> items) {
    }

    /**
     * 创建时间：2026/03/12
     * 功能：定义单个热搜关键词条目。
     * 作者：Devil
     *
     * @param keyword 关键词
     * @param score 热度分数
     */
    public record HotKeyword(String keyword, int score) {
    }
}
