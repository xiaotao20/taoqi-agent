# CLAUDE.md

本文件用于向 Claude Code 及其它开发者说明 taoqi-agent 的项目结构与开发约定，随仓库提交，所有人共享。

## 项目概览

Maven 多模块 Spring Boot 项目，Java 21，Spring Boot 4.1.2，Spring Cloud 2025.1.4（OpenFeign），Spring Cloud Alibaba 2025.1.0.0（Nacos 3.1.1），MyBatis-Plus 3.5.18，MySQL 8。构建使用项目自带的 Maven Wrapper（`./mvnw`），依赖通过项目根目录的 `settings.xml`（阿里云镜像）下载。

| 模块 | 说明 |
|---|---|
| `taoqi-agent` | 父模块，统一管理依赖版本、插件版本与构建配置 |
| `taoqi-agent-common` | 公共能力：`@RateLimit` 限流注解、`R` 统一返回结果、通用工具类 |
| `taoqi-agent-integration` | 第三方服务接入（聚合模块），下分 `-api` 与 `-server` |
| `taoqi-agent-chat` | 聊天服务（聚合模块），下分 `-api` 与 `-server`；含验证码与 AI 异步任务（RocketMQ） |
| `taoqi-agent-log-starter` | 自定义 Spring Boot Starter：请求 traceId 与访问日志 |

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

### Nacos 服务发现与配置中心

- 服务以 `spring.application.name` 为服务名注册到 Nacos；Feign 客户端的 `@FeignClient(name = ...)` 必须与目标服务的 `spring.application.name` 一致，不要再写 `url` 固定地址。
- 消费方同时引入 `spring-cloud-starter-alibaba-nacos-discovery` 和 `spring-cloud-starter-loadbalancer`：前者负责注册与发现，后者负责从实例列表中选择实例。
- 本地开发默认注册 `127.0.0.1`（`spring.cloud.nacos.discovery.ip`），避免多网卡或 IPv6 注册到不可达地址；部署环境用 `NACOS_REGISTER_IP` 覆盖。Nacos 地址、命名空间、分组分别用 `NACOS_ADDR`、`NACOS_NAMESPACE`、`NACOS_GROUP` 覆盖。
- 配置中心 Data ID 使用 `${spring.application.name}.yml`，Group 默认 `DEFAULT_GROUP`，通过 `spring.config.import: optional:nacos:...` 加载；Nacos 配置优先级高于本地 `application.yml`。
- 需要动态刷新的 `@ConfigurationProperties` 使用可变类 + `@RefreshScope`：CGLIB 作用域代理无法继承 final 的 record；线程池等启动期资源不做动态刷新，修改后需要重启。
- 单元测试/上下文测试同时关闭注册和远端配置：`spring.cloud.nacos.discovery.enabled=false`、`spring.cloud.nacos.config.enabled=false`、`spring.cloud.nacos.config.import-check.enabled=false`。

### 代码风格

- 每个 Java 类（含测试类）都要有类级 Javadoc：一句话说明职责，并带 `@author xiaotao` 和 `@date yyyy-MM-dd`。
- Javadoc 中引用类型名用 `{@code Xxx}`，不要用 Markdown 反引号。
- 源码与构建统一 UTF-8。

### 接口返回

- HTTP 路径按服务划分前缀：`/api/chat/**`、`/api/integration/**`，再按业务分组（如 `/api/integration/email/**`）。不要用具体业务作为根路径（如 `/api/email`），同一服务后续接入更多能力时无需再调整网关和反向代理。
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

### 限流

- `@RateLimit` 默认走内存存储，只对单实例有效；多实例通过 `taoqi.rate-limit.store-type=redis` 切换为 Redis 共享计数，需要运行期引入 `spring-boot-starter-data-redis`。
- Redis 实现用 Lua 原子完成自增、设置过期和读取 TTL；Redis 不可用时 fail-open 放行，避免拖垮业务。
- 验证码默认存 Redis（`taoqi.chat.verification-code.store-type=redis`），多实例共享；`memory` 实现基于 Caffeine，仅用于单实例或测试。Redis 校验用 Lua 脚本原子比较并删除，只有验证成功才失效；Redis 不可用时发码接口 fail-closed。

### 异步任务

- 耗时的外部调用（SMTP、短信等）采用异步：接口先落一条 `SENDING` 状态的记录并立即返回，业务层发布事件（如 `EmailSendEvent`），监听器用 `@Async` + `@EventListener` 在自定义 `ThreadPoolTaskExecutor` 中调用执行组件；完成后把状态更新为 `SUCCESS` / `FAILED` 并写入完成时间。
- 状态用枚举定义（如 `EmailSendStatus`），数据库以枚举名存储。
- 线程池要有界并配置拒绝策略（`CallerRunsPolicy`），关闭时等待在途任务完成；内存队列不保证进程重启后任务不丢。需要跨进程可靠投递的任务（如 AI 任务队列）走 RocketMQ。
- 当前阶段暂不引入 outbox、消息队列或定时补偿任务，需要更强保证时再演进。邮件发送仍保留事件 + 线程池；AI 任务已使用 RocketMQ，作为 MQ 接入的示范链路。

### 消息队列

- 消费者使用 `@RocketMQMessageListener`（`rocketmq-spring-boot-starter`，版本在根 pom 单独管理），生产端使用 starter 自动装配的 `RocketMQTemplate`；不要用 Spring Cloud Stream binder 承担需要重投的消费逻辑——其错误通道会吞掉消费者异常，消息仍被 ack。starter 2.3.4 已在 Spring Boot 4.1.2 验证可用。
- `onMessage` 抛出异常即触发 RocketMQ 重投；`maxReconsumeTimes` 是注解常量，不支持属性占位符，调整时同步修改注释。
- 新增 MQ 链路时提供 `dispatch-mode` 开关：`mq` 走 RocketMQ，`local` 走本机线程池（单元测试与本地无 MQ 时使用），通过 `@ConditionalOnProperty` 装配生产者/消费者。
- 消息体只携带业务主键（如 `taskId`），消费端按主键读库；不要把大文本、完整 DTO 塞进消息。
- 消费端必须幂等：用 `where id = ? and status = ?` 条件更新推进状态，重复消费或并发消费时只有一次能成功。
- 消费失败时先落 `FAILED` 并累加失败次数，再抛出异常交给 MQ 重投；超过 `maxReconsumeTimes` 后进入死信队列。
- 死信消费者订阅 `%DLQ%<consumer-group>`，把死信落入 `agent_task_dead_letter`（`msg_id` 唯一索引保证幂等），任务推进到 `DEAD` 终态；`SUCCESS` 不降级，无法解析的消息记为 `IGNORED`。
- 人工重放采用“新建任务”的方式，原任务保留 `DEAD` 作为历史；死信状态用 `PENDING → REPLAYING` 条件更新抢占，防止并发重复重放。
- traceId 放在消息用户属性（`traceId`）中透传，消费端写回 MDC，异步日志才能串起原请求。
- MQ 地址与生产者组写在 `rocketmq.name-server`、`rocketmq.producer.group`，topic 与消费组写在 `taoqi.chat.agent-task.*`，全部用环境变量覆盖；不要只放 Nacos，避免其它开发者不知道需要哪些变量。

### 自定义 Starter

- Starter 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置类（Boot 3+ 机制，不再用 `spring.factories`）。
- 自动配置类使用 `@AutoConfiguration` 加条件注解：`@ConditionalOnClass`、`@ConditionalOnWebApplication`、`@ConditionalOnProperty`、`@ConditionalOnMissingBean`（允许使用方覆盖）。
- 非必需依赖（如 Web 环境）声明为 `<optional>true</optional>`，配置元数据依赖 `spring-boot-configuration-processor`。

## 运行环境

| 服务 | 端口 | 说明 |
|---|---|---|
| Nacos（单机） | 8848、9848-9849；控制台 8080 | 服务注册、发现与配置中心，内嵌 Derby，不需要外部数据库 |
| RocketMQ | nameserver 9876、broker 10909/10911、dashboard 8180 | AI 任务队列；本地可用 `docker/rocketmq` 一键启动，无需 RocketMQ 时把 `AGENT_TASK_DISPATCH_MODE` 设为 `local` |
| `taoqi-agent-integration-server` | 8081 | 邮件服务，依赖本地 MySQL 3306 的 `taoqi-agent-integration` 库 |
| `taoqi-agent-chat-server` | 8082 | 通过 Nacos + Feign 调用邮件服务；AI 任务使用 MySQL 的 `taoqi-agent-chat` 库 |

- Nacos 默认地址 `localhost:8848`，启动方式见 README；两个服务启动后可在控制台「服务管理 → 服务列表」看到 `taoqi-agent-integration` 和 `taoqi-agent-chat`。

- 数据库连接默认 `root` / 环境变量 `MYSQL_USERNAME`、`MYSQL_PASSWORD`；启动时会自动执行 `resources/db/schema.sql`（`CREATE ... IF NOT EXISTS`），JDBC URL 带 `createDatabaseIfNotExist=true`。
- 各服务使用独立数据库：integration 为 `taoqi-agent-integration`，chat 为 `taoqi-agent-chat`，避免表与生命周期耦合。
- 单元测试不依赖本地 MySQL，integration-server 的测试用 H2 内存库（`src/test/resources/application.yml` 与 `schema.sql`）。
- 邮件 SMTP 授权码等敏感信息请通过环境变量注入（`QQ_MAIL_USERNAME`、`QQ_MAIL_AUTH_CODE`），不要提交到仓库。
