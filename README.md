# Skills API

基于 Spring Boot 3 的轻量级 API 脚手架，内置请求追踪、幂等控制、分布式限流等基础设施，并提供多平台热搜数据采集、多分类早报采集、天气预报查询、散文随机句子、违禁词检测、IP 地理位置查询、今日黄历、验证码生成校验、图片格式转换等实用功能。

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.5.11 | Web 框架 |
| Redisson | 3.46.0 | Redis 客户端（分布式锁、限流、缓存） |
| Hutool | 5.8.40 | 工具库（HTTP、JSON、DFA 词树） |
| Jsoup | 1.18.1 | HTML 解析（天气数据采集） |
| ip2region | 3.3.6 | 离线 IP 地理位置查询 |
| lunar-java | 1.7.7 | 中国农历计算（黄历数据） |
| Batik | 1.18 | SVG 图片处理 |
| NextDoc4j | 1.1.7 | API 文档自动生成 |

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
- **Redis 缓存** — 采集数据缓存至 Redis（2 小时 TTL），查询接口直接读取缓存

### 早报数据采集

- **多分类支持** — 综合、财经、科技、体育、国际、汽车、游戏 7 大分类早报数据采集
- **数据来源** — 腾讯新闻早报 API，适配新版 JSON 结构（data.tabs[].articleList[]）
- **自动调度** — 基于 Cron 表达式的可配置定时任务，各分类独立调度
- **Redis 缓存** — 采集数据缓存至 Redis（2 小时 TTL），缓存为空时自动触发即时采集

### 股票指数行情

- **多市场支持** — A股（上证/深证/创业板/沪深300/上证50/中证500/科创50）、港股（恒生/恒生科技）、美股（道琼斯/纳斯达克/标普500）
- **数据来源** — 养基宝 API（app-api.yangjibao.com），实时行情数据
- **丰富指标** — 当前价、涨跌额、涨跌幅、成交额、涨跌家数、平盘家数
- **Redis 缓存** — 数据缓存至 Redis（5 分钟 TTL），平衡实时性与性能
- **按需筛选** — 从全量数据中按配置的指数代码筛选返回，避免无效数据

### 天气预报查询

- **全面数据采集** — 从中国天气网采集实时天气、7 天预报、逐小时预报、24 小时观测和生活指数
- **实时天气** — 温度、天气状况、风向风力、湿度、降水量、气压（来自 d1.weather.com.cn 接口）
- **分日预报** — 7 天预报含白天/夜间天气、温度高低、风向风力、日出日落
- **逐小时预报** — 每日逐小时温度、天气、风向风力变化趋势
- **24 小时观测** — 过去 24 小时整点温度、湿度、风力实测数据
- **生活指数** — 紫外线、穿衣、洗车、运动、过敏、空气污染扩散等
- **两级缓存** — Redis 缓存（10 分钟 TTL）+ 内存城市编码映射，首次查询后极速响应
- **接口限流** — 基于 `@RateLimited` 注解限制天气查询 30 次/分钟

### 散文随机句子

- **散文集** — 收录《我在人间凑数的日子》107 条经典句子
- **JSON 数据源** — 启动时从 `prose-sentences.json` 加载到内存，无需数据库
- **随机返回** — 每次请求随机返回一条句子，适合用作每日一句、签名档等场景

### 违禁词检测

- **DFA 高效匹配** — 基于 Hutool `WordTree`（确定性有限自动机）实现多模式匹配，性能远优于逐词遍历
- **启动预加载** — 应用启动时从 `sensitive-words.json` 加载全部违禁词构建内存词树，检测请求无需外部依赖
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

### 验证码生成与校验

- **多种类型** — 支持线干扰、圆圈干扰、扭曲干扰、GIF 动态验证码
- **可配置参数** — 支持自定义验证码有效期（60-3600秒）、字符数（4-8位）、图片尺寸
- **Redis 存储** — 验证码存储至 Redis，支持分布式部署
- **一次性使用** — 验证码校验后自动删除，防止重复使用
- **忽略大小写** — 校验时忽略大小写，提升用户体验

### 图片格式转换

- **多输入方式** — 支持文件上传、Base64 编码、URL 远程获取三种输入方式
- **SVG 转换** — 基于 Apache Batik 实现 SVG 转 PNG/JPG/WEBP
- **光栅图片转换** — 支持 PNG/JPG/WEBP 之间的格式互转
- **尺寸调整** — 支持指定宽度/高度，可按比例自动计算
- **质量控制** — 对于 JPG 等有损格式，支持自定义压缩质量（1-100）
- **灵活输出** — 支持直接返回二进制流或 Base64 编码的 JSON 响应

## 项目结构

```
src/main/java/ai/skills/api
├── common                              # 公共基础设施
│   ├── api                             #   统一响应结构（ApiResponse、ResponseCode）
│   ├── config                          #   配置管理（Redis、WebMvc、属性类）
│   ├── exception                       #   全局异常处理
│   ├── idempotency                     #   幂等控制（注解 + 拦截器 + Redis 存储）
│   ├── ratelimit                       #   分布式限流（注解 + 拦截器 + Redis 存储）
│   ├── redis                           #   Redis 工具类（锁、缓存、发布订阅）
│   └── web                             #   Web 层（链路追踪 Filter）
├── hotsearch                           # 热搜采集模块
│   ├── collector                       #   平台采集器（百度、微博、抖音、头条、B站）
│   ├── config                          #   调度配置
│   ├── controller                      #   查询接口
│   └── service                         #   业务逻辑（Redis 缓存 + 查询）
├── morningnews                         # 早报采集模块
│   ├── collector                       #   分类采集器（综合、财经、科技、体育、国际、汽车、游戏）
│   ├── config                          #   调度配置
│   ├── controller                      #   查询接口
│   └── service                         #   业务逻辑（Redis 缓存 + 查询）
├── stockindex                          # 股票指数模块
│   ├── collector                       #   指数采集器（养基宝 API）
│   ├── config                          #   配置属性（分组、指数代码）
│   ├── controller                      #   查询接口
│   ├── model                           #   响应 Record（IndexQuote、StockIndexResult）
│   └── service                         #   业务逻辑（Redis 缓存 + 分组筛选）
├── weather                             # 天气预报模块
│   ├── collector                       #   数据采集器（weather.com.cn + d1 接口）
│   ├── controller                      #   查询接口
│   └── model                           #   响应 Record（含 6 个嵌套数据类型）
├── prose                               # 散文随机句子模块
│   ├── controller                      #   查询接口
│   ├── model                           #   响应 model
│   └── service                         #   业务逻辑（JSON 数据加载 + 随机查询）
├── sensitive                           # 违禁词检测模块
│   ├── controller                      #   检测接口
│   ├── model                           #   请求/响应 Record
│   └── service                         #   业务逻辑（DFA 词树 + 文本检测）
├── ip                                  # IP 地理位置查询模块
│   ├── controller                      #   查询接口
│   ├── model                           #   响应 Record
│   └── service                         #   业务逻辑（ip2region 离线查询）
├── fortune                             # 今日黄历模块
│   ├── controller                      #   查询接口
│   ├── model                           #   响应 Record
│   └── service                         #   业务逻辑（农历/宜忌/吉凶方位）
├── captcha                             # 验证码模块
│   ├── controller                      #   生成/校验接口
│   ├── model                           #   枚举/请求/响应 Record
│   └── service                         #   业务逻辑（Hutool Captcha + Redis 存储）
└── image                               # 图片转换模块
    ├── config                          #   配置属性
    ├── controller                      #   转换接口
    ├── model                           #   枚举/响应 Record
    └── service                         #   业务逻辑（SVG/光栅图片转换）

src/main/resources
├── application.yaml                    # 主配置文件
├── data/
│   ├── city-codes.json                 # 城市编码映射（天气查询）
│   ├── prose-sentences.json            # 散文句子数据
│   └── sensitive-words.json            # 违禁词词库
└── ip2region/
    └── ip2region.xdb                   # 离线 IP 地理位置数据库
```

## 快速开始

### 环境要求

- JDK 21+
- Redis 6.x+

### 1. 本地配置

创建 `config/application-local.yaml`（已被 Git 排除）：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
      database: 15
```

### 2. 启动

```bash
./mvnw spring-boot:run
```

### 3. API 文档

项目集成了 **NextDoc4j** API 文档框架，启动成功后访问：
- 文档页面：http://localhost:8080/doc.html

#### 文档注解说明

| 注解 | 位置 | 说明 |
|------|------|------|
| `@Tag` | Controller 类 | 接口分组名称 |
| `@Operation` | Controller 方法 | 接口详细描述 |
| `@Schema` | Model 类/字段 | 数据模型描述 |
| `@Hidden` | Controller/方法 | 隐藏接口（如健康检查） |

### 4. 验证

```bash
# 获取百度热搜
curl http://localhost:8080/api/v1/hot-search/baidu/latest

# 获取微博热搜
curl http://localhost:8080/api/v1/hot-search/weibo/latest

# 获取抖音热搜
curl http://localhost:8080/api/v1/hot-search/douyin/latest

# 获取头条热榜
curl http://localhost:8080/api/v1/hot-search/toutiao/latest

# 获取综合早报
curl http://localhost:8080/api/v1/morning-news/general/latest

# 获取财经早报
curl http://localhost:8080/api/v1/morning-news/finance/latest

# 获取科技早报
curl http://localhost:8080/api/v1/morning-news/tech/latest

# 获取所有股票指数
curl http://localhost:8080/api/v1/stock-index

# 获取指定分组股票指数（A股）
curl http://localhost:8080/api/v1/stock-index/a-share

# 获取港股指数
curl http://localhost:8080/api/v1/stock-index/hk

# 获取美股指数
curl http://localhost:8080/api/v1/stock-index/us

# 天气预报查询
curl http://localhost:8080/api/v1/weather/北京

# 随机散文句子
curl http://localhost:8080/api/v1/prose/random

# 违禁词检测
curl "http://localhost:8080/api/v1/sensitive/check?text=免费领取大奖，加微信了解详情"

# IP 地理位置查询（传入指定 IP）
curl "http://localhost:8080/api/v1/ip/query?ip=113.92.157.29"

# IP 地理位置查询（自动获取调用者 IP）
curl "http://localhost:8080/api/v1/ip/query"

# 今日黄历
curl http://localhost:8080/api/v1/almanac/almanac

# 验证码生成（默认配置）
curl http://localhost:8080/api/v1/captcha/generate

# 验证码生成（自定义配置）
curl -X POST -H "Content-Type: application/json" -d '{"type":"circle","ttl":600,"length":6,"width":150,"height":50}' http://localhost:8080/api/v1/captcha/generate

# 验证码校验
curl -X POST -H "Content-Type: application/json" -d '{"captchaId":"your-captcha-id","captcha":"ABCD"}' http://localhost:8080/api/v1/captcha/verify

# 图片格式转换（文件上传）
curl -X POST -F "file=@test.svg" -F "format=PNG" "http://localhost:8080/api/v1/image/convert" --output converted.png

# 图片格式转换（Base64 输入，Base64 输出）
curl -X POST -d "base64=$(cat image.b64)" -d "format=JPG" -d "quality=80" -d "output=base64" "http://localhost:8080/api/v1/image/convert"

# 图片格式转换（URL 输入，指定尺寸）
curl -X POST -d "url=https://example.com/image.png" -d "format=JPG" -d "width=800" "http://localhost:8080/api/v1/image/convert" --output converted.jpg
```

## API 接口

### 热搜查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/hot-search/baidu/latest` | 获取百度最新热搜 |
| GET | `/api/v1/hot-search/weibo/latest` | 获取微博最新热搜 |
| GET | `/api/v1/hot-search/douyin/latest` | 获取抖音最新热搜 |
| GET | `/api/v1/hot-search/toutiao/latest` | 获取今日头条最新热搜 |

### 天气预报查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/weather/{city}` | 根据城市名查询完整天气数据 |

### 股票指数行情

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/stock-index` | 获取所有分组股票指数 |
| GET | `/api/v1/stock-index/{group}` | 获取指定分组股票指数（a-share/hk/us） |

**响应示例：**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
  "data": [
    {
      "group": "a-share",
      "groupName": "A股指数",
      "quotes": [
        {
          "code": "000001",
          "name": "上证指数",
          "currentPrice": "4038.01",
          "priceChange": "-11.90",
          "changePercent": "-0.29",
          "turnover": "635300591805.9",
          "upCount": "1090",
          "downCount": "1206",
          "flatCount": "66",
          "date": "2026-03-18",
          "time": "13:39:47"
        }
      ],
      "collectedAt": "2026-03-18T13:39:50"
    }
  ],
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-18T05:40:00Z"
}
```

**响应示例：**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
  "data": {
    "city": "北京",
    "cityCode": "101010100",
    "updateTime": "18:00",
    "current": {
      "temp": "10.5",
      "weather": "晴",
      "windDirection": "南风",
      "windPower": "2级",
      "humidity": "39%",
      "rain": "0",
      "pressure": "1018",
      "time": "22:50"
    },
    "forecast": [
      {
        "date": "16日",
        "dayWeather": "晴",
        "nightWeather": "多云",
        "tempHigh": "14",
        "tempLow": "2",
        "dayWindDirection": "南风",
        "dayWindPower": "3-4级",
        "nightWindDirection": "南风",
        "nightWindPower": "<3级",
        "sunrise": "06:22",
        "sunset": "18:22",
        "hourly": [
          { "time": "20:00", "weather": "晴", "temp": "10", "windDirection": "南风", "windPower": "<3级" }
        ],
        "lifeIndices": [
          { "name": "紫外线", "level": "弱", "description": "辐射较弱，涂擦SPF12-15、PA+护肤品。" },
          { "name": "穿衣", "level": "较冷", "description": "建议着厚外套加毛衣等服装。" }
        ]
      }
    ],
    "observations": [
      { "hour": "22", "temp": "8.2", "windDirection": "东南风", "windPower": "2", "humidity": "44" }
    ]
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-16T15:00:00Z"
}
```

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
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
  "data": {
    "hasSensitive": true,
    "foundWords": ["免费领取", "加微信"],
    "filteredText": "****大奖，***了解详情"
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-13T02:00:00Z"
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
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
  "data": {
    "ip": "113.92.157.29",
    "country": "中国",
    "province": "广东省",
    "city": "深圳市",
    "isp": "电信"
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-13T02:00:00Z"
}
```

### 今日黄历

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/almanac/almanac` | 获取今日黄历 |

**响应示例：**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
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
  "timestamp": "2026-03-13T02:00:00Z"
}
```

### 验证码生成与校验

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/captcha/generate` | 生成验证码（默认配置） |
| POST | `/api/v1/captcha/generate` | 生成验证码（自定义配置） |
| POST | `/api/v1/captcha/verify` | 校验验证码 |

**生成验证码请求参数（POST，JSON Body）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 验证码类型：line（线干扰，默认）、circle（圆圈干扰）、shear（扭曲干扰）、gif（GIF 动态） |
| ttl | Integer | 否 | 有效期（秒），60-3600，默认 300 |
| length | Integer | 否 | 验证码位数，4-8，默认 4 |
| width | Integer | 否 | 图片宽度（像素），默认 120 |
| height | Integer | 否 | 图片高度（像素），默认 40 |

**生成验证码响应示例：**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
  "data": {
    "captchaId": "550e8400-e29b-41d4-a716-446655440000",
    "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg..."
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-17T02:00:00Z"
}
```

**校验验证码请求参数（POST，JSON Body）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| captchaId | String | 是 | 验证码唯一标识 |
| captcha | String | 是 | 用户输入的验证码 |

**校验验证码响应示例：**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
  "data": {
    "valid": true,
    "message": "验证码校验通过"
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-17T02:00:00Z"
}
```

### 图片格式转换

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/image/convert` | 转换图片格式 |

**请求参数（multipart/form-data）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 否* | 上传的图片文件 |
| base64 | String | 否* | Base64 编码的图片内容 |
| url | String | 否* | 图片 URL 地址 |
| format | String | 是 | 目标格式：PNG/JPG/WEBP |
| width | Integer | 否 | 输出宽度（像素） |
| height | Integer | 否 | 输出高度（像素） |
| quality | Integer | 否 | 压缩质量 1-100，默认 85 |
| output | String | 否 | 输出方式：binary（默认）/ base64 |

*file、base64、url 三选一，至少提供一个

**响应示例（output=base64）：**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
  "data": {
    "base64": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB...",
    "format": "PNG",
    "width": 800,
    "height": 600,
    "size": 12345
  },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-16T02:00:00Z"
}
```

**响应示例（output=binary）：**

直接返回图片二进制流，Content-Type 为 `image/png`、`image/jpeg` 等。

### 响应格式

所有接口统一包装为以下格式：

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "status": "SUCCESS",
  "data": { ... },
  "traceId": "a1b2c3d4e5f6",
  "timestamp": "2026-03-13T02:00:00Z"
}
```

## 调度配置

在 `application.yaml` 中配置各平台的采集频率：

```yaml
skills-api:
  scheduler:
    enabled: true
    thread-pool-size: 4
    platforms:
      baidu:
        enabled: true
        cron: "0 0/30 * * * ?"   # 每 30 分钟
      weibo:
        enabled: true
        cron: "0 0/30 * * * ?"   # 每 30 分钟
      douyin:
        enabled: true
        cron: "0 0/30 * * * ?"   # 每 30 分钟
      toutiao:
        enabled: true
        cron: "0 0/30 * * * ?"   # 每 30 分钟
      bilibili:
        enabled: true
        cron: "0 */30 * * * ?"   # 每 30 分钟
  morning-news:
    enabled: true
    thread-pool-size: 2
    categories:
      general:
        enabled: true
        cron: "0 0 7 * * ?"      # 每天 7:00
      finance:
        enabled: true
        cron: "0 0 7 * * ?"
      tech:
        enabled: true
        cron: "0 0 7 * * ?"
      sports:
        enabled: true
        cron: "0 0 7 * * ?"
      international:
        enabled: true
        cron: "0 0 7 * * ?"
      auto:
        enabled: true
        cron: "0 0 7 * * ?"
      game:
        enabled: true
        cron: "0 0 7 * * ?"
  stock-index:
    enabled: true
    cache-ttl: PT5M
    groups:
      a-share:
        name: A股指数
        enabled: true
        codes:
          - "000001"
          - "399001"
          - "399006"
          - "000300"
          - "000016"
          - "000905"
          - "000688"
      hk:
        name: 港股指数
        enabled: true
        codes:
          - "HSI"
          - "HSTECH"
      us:
        name: 美股指数
        enabled: true
        codes:
          - "DJI"
          - "IXIC"
          - "SPX"
  image:
    max-file-size: 10485760      # 10MB
    max-output-size: 52428800    # 50MB
    default-quality: 85
```

## 开源协议

本项目基于 [MIT License](LICENSE) 开源。
