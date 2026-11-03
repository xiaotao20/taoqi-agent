# taoqi-agent

一个使用 Java 开发的 Agent 项目。

## 项目简介

taoqi-agent 采用 Maven 多模块结构，以 Spring Boot 为基础框架，核心模块提供 Agent 运行所需的公共能力。项目统一使用 JDK 21 构建，依赖版本由父模块集中管理，便于后续按能力拆分模块、扩展新的 Agent 功能。

## 技术栈

| 类别 | 技术 | 版本 |
|---|---|---|
| 语言 | Java | 21 |
| 构建工具 | Maven（随项目提供 Maven Wrapper） | 3.9.11 |
| 基础框架 | Spring Boot | 4.1.2 |
| 服务通信 | Spring Cloud（OpenFeign） | 2025.1.4（OpenFeign 5.0.4） |
| 邮件发送 | Spring Boot Mail（SMTP） | 由 spring-boot-starter-mail 管理 |
| 单元测试 | JUnit Jupiter | 6.0.3（由 spring-boot-starter-test 管理） |
| 断言库 | AssertJ | 3.27.7（由 spring-boot-starter-test 管理） |
| Mock 框架 | Mockito | 5.23.0（由 spring-boot-starter-test 管理） |
| 编码 | UTF-8 | |

依赖下载通过项目内置的 `settings.xml` 配置阿里云镜像加速。

## 功能模块

| 模块 | 说明 |
|---|---|
| `taoqi-agent` | 父模块（pom），统一管理依赖版本、插件版本与构建配置 |
| `taoqi-agent-common` | 公共模块：通用工具类、常量、公共配置等 |
| `taoqi-agent-integration` | 第三方服务接入聚合模块（pom），下分接口契约与服务实现 |
| └ `taoqi-agent-integration-api` | 接口契约：邮件 Feign 客户端与 DTO，供其它服务直接引入 |
| └ `taoqi-agent-integration-server` | 服务实现：QQ 邮件发送、REST 接口，可独立启动 |

> 后续将随 Agent 能力演进新增模块（如 Agent 核心运行时、模型接入、工具调用、记忆管理等），新增模块的依赖版本仍由父模块统一管理。

## 项目结构

```
taoqi-agent
├── pom.xml                     # 父模块：依赖版本统一管理
├── settings.xml                # Maven 镜像配置（阿里云）
├── .mvn/                       # Maven Wrapper 及全局命令行参数
├── mvnw / mvnw.cmd             # Maven Wrapper 启动脚本
├── taoqi-agent-common          # 公共模块
│   └── src
│       ├── main/java/com/taoqi/common
│       │   └── util/Calculator.java
│       └── test/java/com/taoqi/common
│           └── util/CalculatorTest.java
└── taoqi-agent-integration     # 第三方服务接入模块（聚合 pom）
    ├── pom.xml
    ├── taoqi-agent-integration-api    # 接口契约（Feign + DTO）
    │   └── src
    │       ├── main/java/com/taoqi/integration
    │       │   ├── api/            # EmailApi 接口契约与 DTO
    │       │   └── client/         # EmailFeignClient
    │       └── test/java/com/taoqi/integration/client
    └── taoqi-agent-integration-server # 服务实现（可独立启动）
        └── src
            ├── main/java/com/taoqi/integration
            │   ├── IntegrationApplication.java
            │   ├── config/         # 配置属性
            │   ├── controller/     # REST 接口
            │   ├── exception/      # 全局异常处理
            │   └── service/        # 邮件发送逻辑
            ├── main/resources/application.yml
            └── test/java/com/taoqi/integration
```

## 环境要求

- JDK 21
- 无需单独安装 Maven，使用项目自带的 Maven Wrapper 即可

## 构建与测试

```bash
# 编译
./mvnw clean compile

# 运行全部单元测试
./mvnw test

# 打包
./mvnw clean package

# 只构建指定模块
./mvnw -pl taoqi-agent-common package
```

测试报告输出在 `target/surefire-reports/` 目录下。

## 第三方接入模块

第三方接入采用“聚合模块 + 接口/实现分离”的结构，`taoqi-agent-integration` 与 `taoqi-agent-common` 平级，其下再分：

- `taoqi-agent-integration-api`：接口契约模块，包含 Feign 客户端、接口定义与 DTO，其它服务引入该模块即可调用，不依赖服务端实现。
- `taoqi-agent-integration-server`：服务实现模块，基于 QQ 邮箱 SMTP 提供发信能力并暴露 REST 接口，可独立启动。

目前支持 QQ 邮件发送，后续可继续扩展短信、验证码等通道。

### 配置

邮件账号配置在 `taoqi-agent-integration/taoqi-agent-integration-server/src/main/resources/application.yml`：

| 配置项 | 说明 |
|---|---|
| `spring.mail.username` / `taoqi.mail.from` | 发件 QQ 邮箱地址，已配置默认值，可通过环境变量 `QQ_MAIL_USERNAME` 覆盖 |
| `spring.mail.password` | QQ 邮箱授权码（非登录密码），通过环境变量 `QQ_MAIL_AUTH_CODE` 覆盖 |

```bash
# 启动服务（默认端口 8081）
./mvnw -pl taoqi-agent-integration/taoqi-agent-integration-server spring-boot:run

# 或使用可执行 jar
java -jar taoqi-agent-integration/taoqi-agent-integration-server/target/taoqi-agent-integration-server-*.jar
```

### REST 接口

```bash
curl -X POST http://localhost:8081/api/email/send \
  -H "Content-Type: application/json" \
  -d '{
        "to": ["someone@example.com"],
        "subject": "测试邮件",
        "content": "<h1>你好</h1>",
        "html": true
      }'
```

### Feign 客户端

其它服务引入 `taoqi-agent-integration-api` 依赖后，在启动类或配置类上添加：

```java
@EnableFeignClients(basePackages = "com.taoqi.integration.client")
```

随后注入 `EmailFeignClient` 即可调用邮件服务，目标地址由 `taoqi.integration.base-url` 配置（默认 `http://localhost:8081`）。

> 授权码属于敏感信息，生产环境请通过环境变量或配置中心注入，不要提交到代码仓库。

## 开发约定

- 依赖版本统一在父模块 `pom.xml` 的 `dependencyManagement` 中管理，子模块引用依赖时不写 `<version>`。
- 新增依赖时，版本属性（如 `<xxx.version>`）和 BOM / 依赖条目写在父模块。
- 服务类模块的拆分方式：与 `taoqi-agent-common` 平级的聚合模块（如 `taoqi-agent-integration`）下再建 `-api`（接口契约、Feign 客户端、DTO）和 `-server`（服务实现）两个子模块，其它服务只需引入 `-api` 模块。
- 源码与构建统一使用 UTF-8 编码。
