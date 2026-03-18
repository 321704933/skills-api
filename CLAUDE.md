# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 常用命令

```bash
# 启动应用（需本地 Redis）
./mvnw spring-boot:run

# 编译
./mvnw compile

# 打包（跳过测试）
./mvnw package -DskipTests

# 运行全部测试（需本地 Redis）
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=GlobalApiInfrastructureTest

# 运行单个测试方法
./mvnw test -Dtest=GlobalApiInfrastructureTest#shouldWrapSuccessfulResponseAndExposeTraceId
```

**前置条件**：本地 Redis（默认 localhost:6379，database 15），可在 `config/application-local.yaml`（Git 已排除）中覆盖配置。

## 架构概述

Spring Boot 3.5.11 + Java 21 的轻量级 API 脚手架。无数据库依赖，数据源为外部 API 采集 + 本地 JSON 文件 + Redis 缓存。

### 模块结构

```
src/main/java/ai/skills/api/
├── common/           # 公共基础设施（统一响应、异常处理、幂等、限流、链路追踪、Redis 工具）
├── hotsearch/        # 多平台热搜采集（百度/微博/抖音/头条/B站，定时调度 + Redis 缓存）
├── morningnews/      # 多分类早报采集（综合/财经/科技/体育/国际/汽车/游戏，腾讯新闻 API + Redis 缓存）
├── stockindex/       # 股票指数行情（养基宝 API，A股/港股/美股指数，Redis 缓存）
├── weather/          # 天气预报（weather.com.cn 采集 + 10 分钟 Redis 缓存）
├── prose/            # 散文随机句子（内存加载 JSON）
├── sensitive/        # 敏感词检测（Hutool DFA 算法，内存加载 JSON）
├── ip/               # IP 地理位置（ip2region 离线库，内存查询）
├── fortune/          # 今日黄历（lunar-java 农历计算）
├── captcha/          # 验证码生成与校验（Hutool Captcha，Redis 存储）
└── image/            # 图片格式转换（SVG/PNG/JPG/WEBP，Apache Batik + ImageIO）
```

每个业务模块遵循 `controller → service → collector/model` 分层，公共设施在 `common/` 下按职责子包组织。

### 核心基础设施（common/）

- **统一响应**：`GlobalResponseBodyAdvice` 自动将返回值包装为 `ApiResponse<T>`（含 success/code/message/status/traceId/timestamp/data）。异常由 `GlobalExceptionHandler` 统一转换为 `ApiErrorResponse`。
- **链路追踪**：`TraceIdFilter` 生成或复用 `X-Trace-Id`，写入 MDC 和 `TraceContext`（ThreadLocal）。
- **幂等控制**：`@Idempotent` 注解 + `IdempotencyInterceptor`（order=0）+ Redis 存储。通过 `X-Idempotency-Key` 请求头识别。
- **分布式限流**：`@RateLimited` 注解 + `RateLimitInterceptor`（order=1）+ Redis 滑动窗口。通过 `X-Forwarded-For` 识别客户端。
- **Redis 工具**：`RedisUtils` 封装 Redisson 操作（锁、限流器、发布订阅、缓存 CRUD、集合操作、事务）。

拦截器注册顺序：幂等（0）→ 限流（1），在 `WebMvcConfig` 中配置。

### 数据策略

| 模块 | 数据来源 | 缓存策略 |
|------|---------|---------|
| hotsearch | 外部 API 定时采集 | Redis，2 小时 TTL |
| morningnews | 腾讯新闻 API 采集 | Redis，2 小时 TTL |
| stockindex | 养基宝 API 实时采集 | Redis，5 分钟 TTL |
| weather | weather.com.cn HTML 解析 | Redis，10 分钟 TTL |
| prose | `resources/data/prose-sentences.json` | 启动时加载到内存 |
| sensitive | `resources/data/sensitive-words.json` | 启动时构建 DFA WordTree |
| ip | `resources/ip2region/ip2region.xdb` | 启动时加载到内存 |
| fortune | lunar-java 计算 | 无缓存，实时计算 |
| captcha | Hutool Captcha 生成 | Redis，可配置 TTL（默认 5 分钟） |
| image | 用户上传/URL/Base64 | 无缓存 |

### 配置前缀

所有自定义配置在 `skills-api` 命名空间下，属性类通过 `@ConfigurationPropertiesScan` 自动注册：

- `skills-api.web` → `WebProperties`（追踪头名称、响应包装开关）
- `skills-api.idempotency` → `IdempotencyProperties`（开关、头名称、TTL）
- `skills-api.rate-limit` → `RateLimitProperties`（开关、客户端标识头、默认窗口/配额）
- `skills-api.scheduler` → `SchedulerProperties`（开关、线程池、平台调度 Cron）
- `skills-api.morning-news` → `MorningNewsProperties`（开关、各分类启用状态、调度 Cron）
- `skills-api.stock-index` → `StockIndexProperties`（开关、缓存 TTL、各分组指数代码配置）
- `skills-api.image` → `ImageConvertProperties`（文件大小限制、默认质量）

## 代码规范

- 注释和日志输出统一使用**中文**
- 所有类必须有类头注释：创建时间、功能描述、作者
- 缩写首次出现时补充中文释义（如 `traceId（链路追踪编号）`）
- 数据模型优先使用 Java `record`
- 使用 Lombok `@RequiredArgsConstructor` 进行构造器注入
- Controller 使用 `@Tag`、`@Operation`、`@Schema` 注解生成 API 文档

## Git 提交规范

格式：`<emoji> <Type>|<中文类型> <中文描述>`

| Emoji | Type | 中文类型 |
|-------|------|---------|
| ✨ | feat | 新功能 |
| 🐛 | fix | Bug 修复 |
| ♻️ | refactor | 代码重构 |
| ✏️ | docs | 文档 |
| ⚡ | perf | 性能优化 |
| ✅ | test | 测试 |
| 💄 | style | 风格 |
| 📦 | build | 打包构建 |
| 🚀 | chore | 工程依赖/工具 |

示例：`✨ Features|新功能 添加天气预报查询模块`

## API 文档

启动后访问 `http://localhost:8080/doc.html`（NextDoc4j 生成）。
