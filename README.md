# Skills API

基于 Spring Boot 3 的轻量级 API 脚手架，内置请求追踪、幂等控制、分布式限流等基础设施，并提供多平台热搜数据采集能力。

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.5.x | Web 框架 |
| MyBatis-Plus | 3.5.16 | ORM 持久层 |
| Redisson | 3.46.0 | Redis 客户端（分布式锁、限流、缓存） |
| MySQL | 8.x | 关系型数据库 |
| Hutool | 5.8.40 | 工具库 |

## 核心功能

### 基础设施

- **统一响应包装** — 所有接口自动包装为 `ApiResponse<T>` 格式，包含状态码、数据、链路追踪 ID 和时间戳
- **链路追踪** — 自动生成 `traceId` 并通过 `X-Trace-Id` 响应头透传，集成 MDC 用于日志关联
- **幂等控制** — 基于 `@Idempotent` 注解 + Redis 实现请求去重，防止重复提交
- **分布式限流** — 基于 `@RateLimited` 注解 + Redis 滑动窗口，支持自定义速率和时间窗口
- **全局异常处理** — 统一捕获业务异常、参数校验异常、系统异常，返回结构化错误响应

### 热搜数据采集

- **多平台支持** — 百度、微博、抖音、今日头条热搜数据定时采集
- **自动调度** — 基于 Cron 表达式的可配置定时任务，各平台独立调度互不影响
- **微博免登录** — 自动生成访客 Cookie，无需手动配置登录凭证
- **两级缓存** — Redis 缓存（2 小时 TTL）+ MySQL 持久化，优先读缓存
- **每日去重** — 同平台每天仅保留最新一批数据，自动清理旧批次

## 项目结构

```
src/main/java/ai/skills/api
├── common                          # 公共基础设施
│   ├── api                         #   统一响应结构（ApiResponse、ResponseCode）
│   ├── config                      #   配置管理（Redis、WebMvc、属性类）
│   ├── exception                   #   全局异常处理
│   ├── idempotency                 #   幂等控制（注解 + 拦截器 + Redis 存储）
│   ├── ratelimit                   #   分布式限流（注解 + 拦截器 + Redis 存储）
│   ├── redis                       #   Redis 工具类（锁、缓存、发布订阅）
│   └── web                         #   Web 层（链路追踪 Filter）
├── demo                            # 演示接口
└── hotsearch                       # 热搜采集模块
    ├── collector                   #   平台采集器（百度、微博、抖音、头条）
    │   └── model                   #     API 响应模型
    ├── config                      #   调度配置
    ├── controller                  #   查询接口
    ├── entity                      #   数据库实体
    ├── mapper                      #   MyBatis-Plus Mapper
    └── service                     #   业务逻辑（持久化 + 缓存）
```

## 快速开始

### 环境要求

- JDK 21+
- MySQL 8.x
- Redis 6.x+

### 1. 创建数据库

```sql
CREATE DATABASE skills_api DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行建表脚本：`src/main/resources/sql/hot_search_record.sql`

### 2. 本地配置

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

### 3. 启动

```bash
./mvnw spring-boot:run
```

### 4. 验证

```bash
# 健康检查
curl http://localhost:8080/api/v1/demo/ping

# 获取百度热搜
curl http://localhost:8080/api/v1/hot-search/baidu/latest

# 获取微博热搜
curl http://localhost:8080/api/v1/hot-search/weibo/latest

# 获取抖音热搜
curl http://localhost:8080/api/v1/hot-search/douyin/latest

# 获取头条热榜
curl http://localhost:8080/api/v1/hot-search/toutiao/latest
```

## API 接口

### 热搜查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/hot-search/{platform}/latest` | 获取指定平台最新热搜 |
| GET | `/api/v1/hot-search/{platform}/history?limit=50&offset=0` | 分页查询历史记录 |

`platform` 可选值：`baidu`、`weibo`、`douyin`、`toutiao`

### 演示接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/demo/ping` | 健康检查 |
| POST | `/api/v1/demo/tasks` | 幂等任务创建（需 `X-Idempotency-Key` 请求头） |
| GET | `/api/v1/demo/hot-search` | 限流演示（60 秒内限 2 次） |

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-13T10:00:00"
}
```

## 调度配置

在 `application.yaml` 中配置各平台的采集频率：

```yaml
skills-api:
  scheduler:
    enabled: true
    platforms:
      baidu:
        enabled: true
        cron: "0 0/30 * * * ?"   # 每 30 分钟
      weibo:
        enabled: true
        cron: "0 0/15 * * * ?"   # 每 15 分钟
      douyin:
        enabled: true
        cron: "0 0/20 * * * ?"   # 每 20 分钟
      toutiao:
        enabled: true
        cron: "0 0/30 * * * ?"   # 每 30 分钟
```

## 开源协议

本项目基于 [MIT License](LICENSE) 开源。
