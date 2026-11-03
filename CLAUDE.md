# CLAUDE.md

本文件用于向 Claude Code 及其它开发者说明 taoqi-agent 的项目结构与开发约定，随仓库提交，所有人共享。

## 项目概览

Maven 多模块 Spring Boot 项目，Java 21，Spring Boot 4.1.2，Spring Cloud 2025.1.4（OpenFeign），MyBatis-Plus 3.5.18，MySQL 8。构建使用项目自带的 Maven Wrapper（`./mvnw`），依赖通过项目根目录的 `settings.xml`（阿里云镜像）下载。

| 模块 | 说明 |
|---|---|
| `taoqi-agent` | 父模块，统一管理依赖版本、插件版本与构建配置 |
| `taoqi-agent-common` | 公共能力：`@RateLimit` 限流注解、`R` 统一返回结果、通用工具类 |
| `taoqi-agent-integration` | 第三方服务接入（聚合模块），下分 `-api` 与 `-server` |
| `taoqi-agent-chat` | 聊天服务（聚合模块），下分 `-api` 与 `-server` |
| `taoqi-agent-starter` | 自定义 Spring Boot Starter：请求 traceId 与访问日志 |

常用命令：

```bash
./mvnw clean package            # 全量构建 + 测试
./mvnw -pl taoqi-agent-common test                     # 只构建某个模块
./mvnw -pl taoqi-agent-chat/taoqi-agent-chat-server package   # 嵌套模块用相对路径或 :artifactId
```

## 开发约定

### 模块与命名

- 模块目录名和 artifactId 统一以 `taoqi-agent-` 开头。
- 服务类模块拆成两部分：与 `taoqi-agent-common` 平级的聚合模块下再建 `-api`（接口契约、Feign 客户端、DTO、实体）和 `-server`（服务实现，可独立启动）；其它服务只引入 `-api`。
- Java 包统一使用 `com.taoqi.<domain>`，例如 `com.taoqi.integration`、`com.taoqi.chat`；Feign 客户端放在 `com.taoqi.<domain>.client`。

### 版本管理

- 第三方依赖版本集中在根 `pom.xml` 的 `dependencyManagement`，子模块声明依赖时不写 `<version>`。
- 内部服务各自拥有独立版本号：服务 `pom.xml` 中写字面量 `<version>`，根 `pom.xml` 用同名属性（如 `taoqi-agent-integration.version`）供 `dependencyManagement` 引用，不要用 `${project.version}` 给所有服务统一版本。
- 升级一个带子模块的服务时，需同步：服务自身 `<version>`、子模块 `<parent>` 的 `<version>`、根 pom 中该服务的版本属性。Maven 要求父 POM 版本是字面量，不能写属性表达式。

### 依赖注入

- Spring Bean 的依赖统一使用 `jakarta.annotation.Resource` 字段注入，不写构造器注入（属性多时构造器参数过长、维护成本高）。
- 单元测试用 `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks` 适配字段注入。
- 不涉及依赖注入的初始化（如构建 Caffeine 缓存）可以保留构造器。

### 代码风格

- 每个 Java 类（含测试类）都要有类级 Javadoc：一句话说明职责，并带 `@author xiaotao` 和 `@date yyyy-MM-dd`。
- Javadoc 中引用类型名用 `{@code Xxx}`，不要用 Markdown 反引号。
- 源码与构建统一 UTF-8。

### 接口返回

- 所有 HTTP 接口与 Feign 契约的响应体统一使用 `com.taoqi.common.result.R<T>`，字段为 `code`、`message`、`data`、`success`。
- 常用静态方法：`R.data(data)`、`R.list(list)`、`R.ok()`、`R.fail(message)`、`R.fail(code, message)`、`R.of(...)`。
- 分页接口直接使用 `R<IPage<T>>`，把 MyBatis-Plus 的分页对象原样作为 `data`，不要另建分页 DTO。
- 内部 service 层保留业务结果对象（如 `EmailSendResult`），只在 controller / facade 边界包 `R`。

### Facade 层

- Facade 按被调用的服务命名，例如 `EmailClientFacade`、`SmsClientFacade`，不要用一个笼统的 `ChatFacade` 混合多个服务。
- Facade 负责调用 Feign client 并拆包 `R<T>`，向上返回业务类型 `T`；远程无数据时返回业务对象自身的失败结果，不要把 null 抛给上层。
- Controller 调用 Facade，再用 `R` 包装响应。

### 持久层

- 简单 CRUD、条件查询、分页使用 MyBatis-Plus（`BaseMapper`、`ServiceImpl`、`LambdaQueryWrapper`、`IPage`）。
- 复杂聚合 SQL 写在 `resources/mapper/*.xml`，通过 `mybatis-plus.mapper-locations` 加载。
- 实体类放在 `-api` 模块的 `entity` 包，类名以 `Entity` 结尾，用 `@TableName` 指定表名、`@TableId(type = IdType.AUTO)` 指定自增主键；`-api` 只依赖 `mybatis-plus-annotation`，不依赖整个 ORM。
- 分页插件需要在配置类中注册 `MybatisPlusInterceptor` + `PaginationInnerInterceptor`，并引入 `mybatis-plus-jsqlparser`。

### 异步任务

- 耗时的外部调用（SMTP、短信等）采用异步：接口先落一条 `SENDING` 状态的记录并立即返回，业务层发布事件（如 `EmailSendEvent`），监听器用 `@Async` + `@EventListener` 在自定义 `ThreadPoolTaskExecutor` 中调用执行组件；完成后把状态更新为 `SUCCESS` / `FAILED` 并写入完成时间。
- 状态用枚举定义（如 `EmailSendStatus`），数据库以枚举名存储。
- 线程池要有界并配置拒绝策略（`CallerRunsPolicy`），关闭时等待在途任务完成；内存队列不保证进程重启后任务不丢。当前阶段暂不引入 outbox、消息队列或定时补偿任务，需要更强保证时再演进。

### 自定义 Starter

- Starter 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置类（Boot 3+ 机制，不再用 `spring.factories`）。
- 自动配置类使用 `@AutoConfiguration` 加条件注解：`@ConditionalOnClass`、`@ConditionalOnWebApplication`、`@ConditionalOnProperty`、`@ConditionalOnMissingBean`（允许使用方覆盖）。
- 非必需依赖（如 Web 环境）声明为 `<optional>true</optional>`，配置元数据依赖 `spring-boot-configuration-processor`。

## 运行环境

| 服务 | 端口 | 说明 |
|---|---|---|
| `taoqi-agent-integration-server` | 8081 | 邮件服务，依赖本地 MySQL 3306 的 `taoqi-agent` 库 |
| `taoqi-agent-chat-server` | 8082 | 通过 Feign 调用邮件服务 |

- 数据库连接默认 `root` / 环境变量 `MYSQL_USERNAME`、`MYSQL_PASSWORD`；启动时会自动执行 `resources/db/schema.sql`（`CREATE ... IF NOT EXISTS`），JDBC URL 带 `createDatabaseIfNotExist=true`。
- 单元测试不依赖本地 MySQL，integration-server 的测试用 H2 内存库（`src/test/resources/application.yml` 与 `schema.sql`）。
- 邮件 SMTP 授权码等敏感信息请通过环境变量注入（`QQ_MAIL_USERNAME`、`QQ_MAIL_AUTH_CODE`），不要提交到仓库。
