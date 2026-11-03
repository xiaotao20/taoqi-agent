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

> 后续将随 Agent 能力演进新增模块（如 Agent 核心运行时、模型接入、工具调用、记忆管理等），新增模块的依赖版本仍由父模块统一管理。

## 项目结构

```
taoqi-agent
├── pom.xml                     # 父模块：依赖版本统一管理
├── settings.xml                # Maven 镜像配置（阿里云）
├── .mvn/                       # Maven Wrapper 及全局命令行参数
├── mvnw / mvnw.cmd             # Maven Wrapper 启动脚本
└── taoqi-agent-common          # 公共模块
    └── src
        ├── main/java/org/example/common
        │   └── util/Calculator.java
        └── test/java/org/example/common
            └── util/CalculatorTest.java
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

## 开发约定

- 依赖版本统一在父模块 `pom.xml` 的 `dependencyManagement` 中管理，子模块引用依赖时不写 `<version>`。
- 新增依赖时，版本属性（如 `<xxx.version>`）和 BOM / 依赖条目写在父模块。
- 源码与构建统一使用 UTF-8 编码。
