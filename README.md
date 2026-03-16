# Skills API

基于 Spring Boot 3 的轻量级 API 脚手架，内置请求追踪、幂等控制、分布式限流等基础设施，并提供多平台热搜数据采集与散文随机句子等趣味功能。

## 技术栈

| 组件 | 版本     | 说明 |
|------|--------|------|
| Java | 21     | LTS 版本 |
| Spring Boot | 3.5.x  | Web 框架 |
| MyBatis-Plus | 3.5.16 | ORM 持久层 |
| Redisson | 3.46.0 | Redis 客户端（分布式锁、限流、缓存） |
| MySQL | 8.x    | 关系型数据库 |
| Hutool | 5.8.40 | 工具库 |
| ip2region | 3.3.6  | 离线 IP 地理位置查询 |
| NextDoc4j | 1.1.7  | API 文档自动生成 |

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

### 散文随机句子

- **散文集** — 收录《我在人间凑数的日子》107 条经典句子
- **随机返回** — 每次请求随机返回一条句子，适合用作每日一句、签名档等场景

### 违禁词检测

- **DFA 高效匹配** — 基于 Hutool `WordTree`（确定性有限自动机）实现多模式匹配，性能远优于逐词遍历
- **启动预加载** — 应用启动时从数据库加载全部违禁词构建内存词树，检测请求无需查库
- **分类管理** — 违禁词支持分类（广告、暴力、色情、诈骗、赌博等），通过 SQL 维护
- **文本过滤** — 返回命中词列表及违禁词替换为 `*` 后的过滤文本

### IP 地理位置查询

- **离线数据库** — 基于 ip2region v3 离线 IP 数据库，纯内存查询，无需外部依赖
- **全内存缓存** — 启动时将 xdb 文件加载到内存，查询性能极高且线程安全
- **自动获取 IP** — 支持传入 IP 地址查询，也可不传参数自动获取调用者 IP
- **完整信息** — 返回国家、省份、城市、运营商等地理位置信息

### 今日黄历

- **万年历信息** — 公历日期、农历日期、干支纪年（年月日干支）
- **生肖信息** — 根据日期自动计算生肖
- **真实宜忌** — 基于 lunar-java 库的真实老黄历数据，每天结果一致
- **详细黄历** — 包含星宿、彭祖百忌、神位方位、冲煞、纳音、月相等完整黄历信息

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
├── hotsearch                       # 热搜采集模块
│   ├── collector                   #   平台采集器（百度、微博、抖音、头条）
│   │   └── model                   #     API 响应模型
│   ├── config                      #   调度配置
│   ├── controller                  #   查询接口
│   ├── entity                      #   数据库实体
│   ├── mapper                      #   MyBatis-Plus Mapper
│   ├── Platform.java               #   平台枚举
│   └── service                     #   业务逻辑（持久化 + 缓存）
├── prose                           # 散文随机句子模块
│   ├── controller                  #   查询接口
│   ├── entity                      #   数据库实体
│   ├── mapper                      #   MyBatis-Plus Mapper
│   └── service                     #   业务逻辑
├── sensitive                       # 违禁词检测模块
│   ├── controller                  #   检测接口
│   ├── entity                      #   数据库实体
│   ├── mapper                      #   MyBatis-Plus Mapper
│   ├── model                       #   请求/响应 Record
│   └── service                     #   业务逻辑（DFA 词树 + 检测）
└── ip                              # IP 地理位置查询模块
    ├── controller                  #   查询接口
    ├── model                       #   响应 Record
    └── service                     #   业务逻辑（ip2region 离线查询）
└── fortune                         # 今日黄历模块
    ├── controller                  #   查询接口
    ├── model                       #   响应 Record
    └── service                     #   业务逻辑（农历/宜忌/吉凶方位）
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

执行建表脚本：`src/main/resources/sql/hot_search_record.sql`、`src/main/resources/sql/prose_sentence.sql`、`src/main/resources/sql/sensitive_word.sql`

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

### 4. API 文档

项目集成了 **NextDoc4j** API 文档框架，启动成功后访问：
- 文档页面：http://localhost:8080/doc.html

#### 文档注解说明

| 注解 | 位置 | 说明 |
|------|------|------|
| `@Tag` | Controller 类 | 接口分组名称 |
| `@Operation` | Controller 方法 | 接口详细描述 |
| `@Schema` | Model 类/字段 | 数据模型描述 |
| `@Hidden` | Controller/方法 | 隐藏接口（如健康检查） |

### 5. 验证

```bash
# 获取百度热搜
curl http://localhost:8080/api/v1/hot-search/baidu/latest

# 获取微博热搜
curl http://localhost:8080/api/v1/hot-search/weibo/latest

# 获取抖音热搜
curl http://localhost:8080/api/v1/hot-search/douyin/latest

# 获取头条热榜
curl http://localhost:8080/api/v1/hot-search/toutiao/latest

# 查询百度指定日期热搜
curl "http://localhost:8080/api/v1/hot-search/baidu/history?date=2026-03-16"

# 随机散文句子
curl http://localhost:8080/api/v1/prose/random

# 违禁词检测
curl "http://localhost:8080/api/v1/sensitive/check?text=免费领取大奖，加微信了解详情"

# IP 地理位置查询（传入指定 IP）
curl "http://localhost:8080/api/v1/ip/query?ip=113.92.157.29"

# IP 地理位置查询（自动获取调用者 IP）
curl "http://localhost:8080/api/v1/ip/query"

# 今日黄历
curl "http://localhost:8080/api/almanac"
```

## API 接口

### 热搜查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/hot-search/baidu/latest` | 获取百度最新热搜 |
| GET | `/api/v1/hot-search/baidu/history?date=2026-03-16` | 查询百度指定日期热搜 |
| GET | `/api/v1/hot-search/weibo/latest` | 获取微博最新热搜 |
| GET | `/api/v1/hot-search/weibo/history?date=2026-03-16` | 查询微博指定日期热搜 |
| GET | `/api/v1/hot-search/douyin/latest` | 获取抖音最新热搜 |
| GET | `/api/v1/hot-search/douyin/history?date=2026-03-16` | 查询抖音指定日期热搜 |
| GET | `/api/v1/hot-search/toutiao/latest` | 获取今日头条最新热搜 |
| GET | `/api/v1/hot-search/toutiao/history?date=2026-03-16` | 查询今日头条指定日期热搜 |

### 散文句子

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/prose/random` | 随机返回一条散文句子 |

### 违禁词检测

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/sensitive/check?text=要检查的文本内容` | 检测文本是否包含违禁词 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hasSensitive": true,
    "foundWords": ["免费领取", "加微信"],
    "filteredText": "****大奖，***了解详情"
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-13T10:00:00"
}
```

### IP 地理位置查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/ip/query?ip=1.2.3.4` | 查询指定 IP 的地理位置 |
| GET | `/api/v1/ip/query` | 自动获取调用者 IP 并查询 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "ip": "113.92.157.29",
    "country": "中国",
    "province": "广东省",
    "city": "深圳市",
    "isp": "电信"
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-13T10:00:00"
}
```

### 今日黄历

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/almanac` | 获取今日黄历 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "date": "2026-03-13",
    "lunarDate": "丙午年二月十五",
    "yearGanZhi": "丙午",
    "monthGanZhi": "辛卯",
    "dayGanZhi": "癸巳",
    "zodiac": "马",
    "suitable": ["祭祀", "祈福", "求嗣", "开光", "塑绘"],
    "avoid": ["开市", "交易", "立券", "动土", "破土", "安葬"],
    "festivals": [],
    "jieQi": "惊蛰",
    "week": "星期五",
    "xiu": "房",
    "xiuLuck": "吉",
    "pengZuGan": "癸不词讼",
    "pengZuZhi": "巳不远行",
    "positionXi": "东南",
    "positionFu": "正东",
    "positionCai": "正南",
    "dayChong": "蛇",
    "daySha": "北",
    "yearNaYin": "天河水",
    "monthNaYin": "石榴木",
    "dayNaYin": "长流水",
    "yueXiang": "满月"
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-13T10:00:00"
}
```

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
