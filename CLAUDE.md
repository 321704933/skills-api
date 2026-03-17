# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 常用命令

```bash
# 启动应用
./mvnw spring-boot:run

# 编译
./mvnw compile

# 打包
./mvnw package -DskipTests

# 运行测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=ClassName

# 运行单个测试方法
./mvnw test -Dtest=ClassName#methodName
```

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.5.11 | Web 框架 |
| MyBatis-Plus | 3.5.16 | ORM 持久层 |
| Redisson | 3.46.0 | Redis 客户端 |
| Hutool | 5.8.40 | 工具库 |
| lunar | 1.7.7 | 农历计算库 |
| ip2region | 3.3.6 | 离线 IP 地理位置 |

## 架构概述

### 模块结构

```
src/main/java/ai/skills/api
├── common/           # 公共基础设施
│   ├── api/          #   统一响应（ApiResponse、ResponseCode、ResponseStatus）
│   ├── config/       #   配置类（Redis、WebMvc、属性绑定）
│   ├── exception/    #   全局异常处理（GlobalExceptionHandler、GlobalResponseBodyAdvice）
│   ├── idempotency/  #   幂等控制（@Idempotent + 拦截器 + Redis 存储）
│   ├── ratelimit/    #   分布式限流（@RateLimited + 拦截器 + Redis 滑动窗口）
│   ├── redis/        #   Redis 工具类（RedisUtils）
│   └── web/          #   Web 层（TraceIdFilter、TraceContext）
├── hotsearch/        # 热搜采集模块
├── prose/            # 散文随机句子模块
├── sensitive/        # 违禁词检测模块（DFA 算法）
├── ip/               # IP 地理位置查询模块（ip2region）
├── fortune/          # 今日黄历模块（lunar-java）
└── weather/          # 天气预报模块（weather.com.cn 采集 + Redis 缓存）
```

### 核心基础设施

**统一响应包装**：所有 API 响应通过 `GlobalResponseBodyAdvice` 自动包装为 `ApiResponse<T>`，包含 `success`、`code`、`message`、`status`、`timestamp`、`traceId`、`data`。

**链路追踪**：`TraceIdFilter` 自动生成 `traceId` 并写入 MDC，通过 `X-Trace-Id` 响应头透传。

**幂等控制**：
- `@Idempotent` 注解标记需要幂等的接口
- `IdempotencyInterceptor` 拦截并校验 `X-Idempotency-Key` 请求头
- `RedisIdempotencyStore` 基于 Redis 实现分布式幂等存储

**分布式限流**：
- `@RateLimited` 注解配置限流规则（permits、window）
- `RateLimitInterceptor` 拦截并执行限流校验
- `RedisRateLimiter` 基于 Redis 滑动窗口实现

### 配置属性

所有配置前缀为 `skills-api`：

```yaml
skills-api:
  web:
    trace-header-name: X-Trace-Id
    wrap-success-response: true
  idempotency:
    enabled: true
    header-name: X-Idempotency-Key
    ttl: PT10M
  rate-limit:
    enabled: true
    client-identifier-header: X-Forwarded-For
    default-window: PT1M
    default-permits: 60
  scheduler:
    enabled: true
    thread-pool-size: 4
```

### 热搜采集器

各平台采集器实现 `HotSearchCollector` 接口，位于 `hotsearch/collector/`：
- `BaiduHotSearchCollector` - 百度热搜
- `WeiboHotSearchCollector` - 微博热搜（自动生成访客 Cookie）
- `DouyinHotSearchCollector` - 抖音热搜
- `ToutiaoHotSearchCollector` - 今日头条热榜

定时调度通过 `HotSearchSchedulerConfig` 配置，各平台独立调度。

### 本地开发配置

创建 `config/application-local.yaml`（已被 Git 排除）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/skills_api?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
      database: 15
```

## 代码规范

详见 `docs/comment-guidelines.md`，核心要求：

- 注释语言统一为中文
- 所有类必须有类头注释（创建时间、功能、作者 Devil）
- 日志输出必须使用中文
- 缩写首次出现时必须补充中文释义

## Git 提交规范

格式：`<icon> 英文|中文 <subject>`

示例：
- `✨ Features|新功能 添加天气采集接口`
- `🐛 Bug Fixes|Bug 修复 修复幂等键重复校验问题`
- `♻️ Refactoring|代码重构 重构限流器实现逻辑`

## API 文档

启动后访问 http://localhost:8080/doc.html（NextDoc4j）

Controller 使用 `@Tag` 标记分组，方法使用 `@Operation` 描述接口，Model 使用 `@Schema` 描述字段。
