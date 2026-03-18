# Skills API Packager

多平台原生打包工具，将 Spring Boot JAR 打包为无需 Java 环境的可执行程序。

## 前置条件

1. **JDK 21+**（需要包含 jpackage 工具的完整 JDK，JRE 不行）
2. **已完成主项目打包**：在主项目目录执行 `./mvnw package -DskipTests`

## 快速使用

### 1. 先打包主项目

```bash
# 在主项目根目录执行
./mvnw clean package -DskipTests
```

### 2. 执行打包

```bash
# 进入 packager 目录
cd packager

# 当前平台打包（自动检测）
mvn package

# 或指定平台
mvn package -P windows   # 仅 Windows
mvn package -P linux     # 仅 Linux
mvn package -P macos     # 仅 macOS
```

### 3. 输出结果

打包完成后，在 `target/packages/` 目录下生成：

| 平台 | 输出路径 | 说明 |
|------|---------|------|
| Windows | `target/packages/windows/skills-api/` | 包含 `skills-api.exe` |
| Linux | `target/packages/linux/skills-api/` | 包含 `skills-api` 可执行文件 |
| macOS | `target/packages/macos/skills-api.app/` | macOS 应用包 |

## 目录结构

打包后的目录结构：

```
skills-api/
├── skills-api.exe          # Windows 可执行文件
├── skills-api              # Linux/macOS 可执行文件
├── app/
│   └── skills-api-1.0.0.jar
├── config/                 # 配置文件目录（可修改）
│   └── application-prod.yaml
├── data/                   # 数据文件（敏感词、散文等）
│   ├── city-codes.json
│   ├── prose-sentences.json
│   └── sensitive-words.json
├── ip2region/              # IP 地理位置库
│   └── ip2region.xdb
└── runtime/                # 内嵌 JRE（自动生成）
```

## 配置说明

### 修改应用配置

打包后的配置文件位于 `config/` 目录，可直接修改：

```yaml
# config/application-prod.yaml
spring:
  data:
    redis:
      host: your-redis-host
      port: 6379
      password: your-password
      database: 0
```

### 启动参数

```bash
# Windows
skills-api.exe

# Linux/macOS
./skills-api

# 指定 JVM 参数（通过环境变量）
JAVA_OPTIONS="-Xmx512m" ./skills-api
```

## 图标文件

各平台图标文件放在 `packager/icons/` 目录：

| 文件 | 平台 | 推荐尺寸 |
|------|------|---------|
| `icon.ico` | Windows | 256x256 |
| `icon.png` | Linux | 512x512 |
| `icon.icns` | macOS | 512x512 |

## 常见问题

### Q: 提示找不到 jpackage 命令？

确保使用的是完整 JDK（不是 JRE），且 JDK 版本 >= 17。jpackage 在 JDK 14 引入，17 正式可用。

```bash
# 检查 jpackage 是否可用
jpackage --version
```

### Q: 打包后体积很大？

这是正常的，因为内嵌了完整的 JRE。典型体积：
- Windows: ~80-100MB
- Linux: ~80-100MB
- macOS: ~80-100MB

如需更小体积，可考虑 GraalVM Native Image，但兼容性较差。

### Q: 如何制作安装包？

修改 `pom.xml` 中的 `--type` 参数：

| 类型 | 说明 |
|------|------|
| `app-image` | 免安装目录（默认） |
| `exe` | Windows 安装程序 |
| `msi` | Windows MSI 安装包 |
| `rpm` | Linux RPM 包 |
| `deb` | Linux DEB 包 |
| `dmg` | macOS 磁盘镜像 |

## 注意事项

1. **跨平台打包**：需要在对应平台上执行打包（Windows 打 Windows 版，Mac 打 Mac 版）
2. **Redis 依赖**：打包后仍需用户自行安装 Redis
3. **配置文件**：首次运行前需修改 `config/` 目录下的配置
