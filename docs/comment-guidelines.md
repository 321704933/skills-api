# 注释规范

## 1. 目标

本规范用于统一 `skills-api` 项目的中文注释风格，保证代码在扩展天气、百度热搜、抖音热搜等采集能力时，依然具备稳定的可读性和可维护性。

注释的核心要求：

- 注释以中文为主。
- 类、接口、枚举、注解、记录类都必须有类头注释。
- 代码中的缩写首次出现时，必须补充中文释义。
- 注释要解释"为什么"和"做什么"，不要机械复述代码字面含义。
- 注释内容要与代码保持同步，修改代码时必须同步更新注释。

---

## 2. 适用范围

本规范适用于以下文件：

- `src/main/java` 下所有生产代码
- `src/test/java` 下所有测试代码
- 后续新增的公共基础设施类、业务模块类、配置类、DTO、VO、枚举、异常类、拦截器、过滤器、工具类

---

## 3. 类头注释规范

所有类都必须使用 Javadoc 风格注释，并包含以下信息：

- 创建时间
- 功能
- 作者

统一模板：

```java
/**
 * 创建时间：当前时间
 * 功能：这里描述类的职责边界，说明这个类解决什么问题。
 * 作者：Devil
 */
public class DemoClass {
}
```

说明：

- `创建时间` 统一写为 `yyyy/MM/dd`
- `功能` 不要写空话，例如"工具类""控制器"等过于宽泛的描述不够，必须说明职责
- `作者` 当前项目统一写 `Devil`

### 3.1 必须添加类头注释的类型

- `class`
- `interface`
- `enum`
- `@interface`
- `record`
- 内部类、内部记录类、内部枚举
- 测试类

---

## 4. 方法注释规范

以下情况必须写方法注释：

- 公共方法
- 业务含义不够直观的方法
- 有副作用的方法
- 影响全局行为的方法
- 测试方法

模板：

```java
/**
 * 功能：说明方法的作用和执行结果。
 *
 * @param request 请求对象
 * @return 响应结果
 */
public Object execute(Request request) {
    return null;
}
```

要求：

- `功能` 说明方法做什么，不需要逐行翻译代码
- `@param` 用中文解释参数用途
- `@return` 用中文解释返回值含义
- `@throws` 在异常对理解有帮助时补充

不推荐的写法：

```java
/**
 * 设置名称
 */
public void setName(String name) {
    this.name = name;
}
```

上面这种注释没有信息增量，属于低质量注释。

---

## 5. 字段与常量注释规范

以下字段建议补充注释：

- 语义不明显的字段
- 有业务约束的字段
- 全局常量
- 缩写字段
- 跨模块共享字段

示例：

```java
/**
 * 默认幂等请求头名称。
 */
private String headerName = "X-Idempotency-Key";

/**
 * TTL（Time To Live，生存时间），表示记录的有效时长。
 */
private Duration ttl = Duration.ofMinutes(10);
```

---

## 6. 缩写注释规范

项目中允许使用缩写，但首次出现时必须补充中文释义。

推荐格式：

```java
API（应用程序编程接口）
HTTP（超文本传输协议）
TTL（Time To Live，生存时间）
MDC（Mapped Diagnostic Context，映射诊断上下文）
DTO（数据传输对象）
VO（视图对象）
ID（唯一标识）
traceId（链路追踪编号）
taskId（任务编号）
```

规则：

- 首次出现时写"英文缩写 + 中文释义"
- 同一文件后续再次出现，可不重复解释
- 新增缩写如果不是行业内极常见术语，必须补注释
- 变量名中出现缩写时，优先在类注释、字段注释或方法注释中解释

不推荐：

```java
private Duration ttl;
private String bizId;
private String dto;
```

如果必须使用这类命名，至少要在注释中解释含义。

---

## 7. 业务注释规范

业务注释重点解释以下内容：

- 为什么要这样设计
- 这段逻辑保护什么边界
- 异常场景如何处理
- 与其他模块的协作关系
- 配置项对行为的影响

示例：

```java
/**
 * 功能：在请求进入控制器前执行幂等校验，避免重复提交。
 */
public class IdempotencyInterceptor implements HandlerInterceptor {
}
```

这类注释是有效的，因为它解释了这个组件存在的目的。

---

## 8. 日志输出规范

### 8.1 日志语言要求

- 所有日志输出必须使用中文
- 日志信息应清晰描述发生的事件和上下文
- 包含关键标识符（如键名、参数等）便于问题定位

### 8.2 推荐格式

```java
// 成功场景 - 可选
log.debug("操作成功，键名：{}", key);

// 错误场景 - 必须有
log.error("Redis 设置缓存失败，键名：{}", key, e);
log.error("幂等键占用失败，键名：{}", key, e);
log.error("限流器操作失败，键名：{}", key, e);
```

### 8.3 日志级别使用

| 级别 | 使用场景 |
|------|----------|
| `TRACE` | 详细的程序运行轨迹，生产环境一般关闭 |
| `DEBUG` | 调试信息，开发/测试环境使用 |
| `INFO` | 关键业务节点、启动/关闭事件 |
| `WARN` | 潜在问题，不影响系统运行 |
| `ERROR` | 错误事件，需要关注和处理 |

### 8.4 日志规范示例

```java
// 推荐：中文描述 + 关键参数
log.error("Redis 分布式锁获取超时，键名：{}", lockKey);
log.error("Redis 获取缓存失败，键名：{}", key, e);
log.debug("消息已发布到 {} 个订阅者，频道：{}", receivers, channelKey);

// 不推荐：英文或无上下文
log.error("Redis error for key: {}", key);
log.error("操作失败");
```

---

## 9. 测试注释规范

测试类和测试方法也要有中文注释，重点说明验证目标。

示例：

```java
/**
 * 创建时间：2026/03/12
 * 功能：验证全局 API 基础设施是否按预期工作。
 * 作者：Devil
 */
class GlobalApiInfrastructureTest {

    /**
     * 功能：验证成功响应会被统一包装，并且返回 traceId。
     */
    @Test
    void shouldWrapSuccessfulResponseAndExposeTraceId() {
    }
}
```

要求：

- 测试方法注释要说明"验证点"
- 不需要把测试步骤逐行翻译成注释
- 对复杂场景可额外说明前置条件和预期结果

---

## 10. 注释质量要求

高质量注释具备以下特征：

- 读者看完注释后，能快速理解职责和边界
- 注释补充了代码无法直接表达的信息
- 注释短而准，不堆砌废话
- 注释和代码保持一致

低质量注释包括：

- 和代码字面完全重复
- 只写"初始化""设置值""返回结果"之类空泛描述
- 注释过期
- 中英文混乱且没有术语说明

---

## 11. 新增代码时的执行要求

后续新增任何 Java 文件时，默认执行以下检查：

1. 是否有类头注释
2. 是否包含创建时间、功能、作者
3. 是否存在未解释的缩写
4. 公共方法是否需要补中文注释
5. 关键配置、常量、异常逻辑是否补了说明
6. 测试类和关键测试方法是否有验证目标注释
7. 日志输出是否使用中文

---

## 12. 推荐写法示例

### 12.1 配置类

```java
/**
 * 创建时间：2026/03/12
 * 功能：Redisson 配置类，统一配置 JSON 序列化器，确保存储到 Redis 的数据可读。
 * 作者：Devil
 */
@Configuration
public class RedissonConfig {
}
```

### 12.2 工具类

```java
/**
 * 创建时间：2026/03/12
 * 功能：Redis 工具类（基于 Redisson），提供分布式锁、限流、缓存等常用操作封装。
 * 说明：序列化配置已在 {@link RedissonConfig} 中统一配置。
 * 作者：Devil
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisUtils {
}
```

### 12.3 拦截器

```java
/**
 * 创建时间：2026/03/12
 * 功能：在请求进入控制器前执行限流校验，并写入限流响应头。
 * 作者：Devil
 */
public class RateLimitInterceptor implements HandlerInterceptor {
}
```

### 12.4 存储实现类

```java
/**
 * 创建时间：2026/03/12
 * 功能：基于 Redis（Redisson）实现幂等存储，支持分布式环境。
 * 作者：Devil
 */
public class RedisIdempotencyStore implements IdempotencyStore {
}
```

### 12.5 记录类

```java
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
```

---

## 13. 提交前检查清单

提交代码前，至少自查以下项目：

**代码注释检查：**
- [ ] 所有新增类都有类头注释
- [ ] 类头注释包含创建时间、功能、作者 Devil
- [ ] 新增缩写都有中文释义
- [ ] 关键方法有中文注释
- [ ] 日志输出使用中文
- [ ] 测试类和核心测试方法有中文说明
- [ ] 注释内容和当前实现一致

**提交规范检查：**
- [ ] 提交类型使用规范规定的类型（feat/fix/docs 等）
- [ ] 提交描述使用中文
- [ ] 提交描述简明扼要（不超过 50 字符）
- [ ] 一个提交只做一件事

---

## 14. 当前项目约定

当前项目执行以下固定约定：

- 注释语言统一为中文
- 日志输出统一为中文
- 作者统一为 `Devil`
- 日期格式统一为 `yyyy/MM/dd`
- 缩写首次出现必须带中文释义
- 基础设施类优先解释职责边界
- 测试注释优先解释验证目标

---

## 15. 技术栈说明

当前项目技术栈：

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.5.11 | Web 框架 |
| Redisson | 3.46.0 | Redis 客户端，提供分布式锁、限流器等 |
| Hutool | 5.8.37 | 工具库 |
| JUnit 5 | - | 测试框架 |

---

## 16. 代码提交规范

### 16.1 提交信息格式

提交信息必须遵循以下格式：

```
<icon> 英文|中文 <subject>
```

- `icon`：对应类型的图标，必填
- `英文|中文`：类型的英文和中文名称，必填
- `subject`：提交描述，必填，使用中文简明描述本次提交的内容

### 16.2 提交类型

| 类型 | 图标 | 英文 | 中文说明 |
|------|------|------|----------|
| `feat` | ✨ | Features | 新功能 |
| `fix` | 🐛 | Bug Fixes | Bug 修复 |
| `init` | 🎉 | Init | 初始化 |
| `docs` | ✏️ | Documentation | 文档 |
| `style` | 💄 | Styles | 风格（不影响代码含义的改动，如格式化） |
| `refactor` | ♻️ | Refactoring | 代码重构 |
| `perf` | ⚡ | Performance | 性能优化 |
| `test` | ✅ | Tests | 测试 |
| `revert` | ⏪ | Revert | 回退 |
| `build` | 📦 | Build System | 打包构建 |
| `chore` | 🚀 | Chore | 工程/工具/依赖变动 |
| `ci` | 👷 | CI | CI 配置变动 |

### 16.3 提交示例

```bash
# 新功能
✨ Features|新功能 添加天气采集接口

# Bug 修复
🐛 Bug Fixes|Bug 修复 修复幂等键重复校验问题

# 初始化
🎉 Init|初始化 初始化项目基础结构

# 文档
✏️ Documentation|文档 更新代码提交规范

# 风格
💄 Styles|风格 格式化代码缩进

# 代码重构
♻️ Refactoring|代码重构 重构限流器实现逻辑

# 性能优化
⚡ Performance|性能优化 优化 Redis 连接池配置

# 测试
✅ Tests|测试 添加限流拦截器单元测试

# 回退
⏪ Revert|回退 回退上一次提交

# 打包构建
📦 Build System|打包构建 更新 Maven 打包配置

# 工程/工具/依赖
🚀 Chore|工程依赖/工具 升级 Spring Boot 版本

# CI 配置
👷 CI|CI 配置 添加 GitHub Actions 工作流
```

### 16.4 提交规范要求

- 提交类型必须使用上述规定的类型之一
- 提交描述必须使用中文
- 提交描述应简明扼要，不超过 50 个字符
- 一个提交只做一件事，避免混合多种类型的改动
- 提交前确保代码可通过编译和测试

---

## 17. 后续扩展

后续如需进一步自动化，可以继续补：

- `Checkstyle` 注释规则
- 提交前静态检查
- 代码模板生成器
