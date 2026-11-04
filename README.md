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
| 服务注册与配置中心 | Spring Cloud Alibaba / Nacos | 2025.1.0.0 / 3.1.1 |
| 邮件发送 | Spring Boot Mail（SMTP） | 由 spring-boot-starter-mail 管理 |
| 持久层 | MyBatis-Plus | 3.5.18（Boot 4 专用 starter） |
| 分布式缓存 | Redis（验证码、限流） | 7.x |
| 本地缓存 | Caffeine（验证码内存实现） | 由 Spring Boot BOM 管理 |
| 数据库 | MySQL（本地） | 8.x |
| 单元测试 | JUnit Jupiter | 6.0.3（由 spring-boot-starter-test 管理） |
| 断言库 | AssertJ | 3.27.7（由 spring-boot-starter-test 管理） |
| Mock 框架 | Mockito | 5.23.0（由 spring-boot-starter-test 管理） |
| 编码 | UTF-8 | |

依赖下载通过项目内置的 `settings.xml` 配置阿里云镜像加速。

## 功能模块

| 模块 | 说明 |
|---|---|
| `taoqi-agent` | 父模块（pom），统一管理依赖版本、插件版本与构建配置 |
| `taoqi-agent-common` | 公共模块：通用工具类、`@RateLimit` 限流注解等通用能力 |
| `taoqi-agent-integration` | 第三方服务接入聚合模块（pom），下分接口契约与服务实现 |
| └ `taoqi-agent-integration-api` | 接口契约：邮件 Feign 客户端与 DTO，供其它服务直接引入 |
| └ `taoqi-agent-integration-server` | 服务实现：QQ 邮件发送、发送记录入库、REST 接口，注册到 Nacos，可独立启动 |
| `taoqi-agent-chat` | 聊天模块聚合模块（pom），下分接口契约与服务实现 |
| └ `taoqi-agent-chat-api` | 聊天模块的接口契约（暂未定义） |
| └ `taoqi-agent-chat-server` | 聊天模块的服务实现：通过 Nacos + Feign 调用邮件服务，默认端口 8082 |
| `taoqi-agent-starter` | 自定义 Spring Boot Starter：引入即自动为 Servlet 应用加上 traceId 与访问日志 |

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
│       │   ├── annotation/       # @RateLimit 限流注解
│       │   ├── aspect/           # RateLimitAspect 切面实现
│       │   ├── autoconfigure/    # RateLimitAutoConfiguration 自动配置
│       │   ├── exception/        # RateLimitExceededException
│       │   ├── result/           # R 统一返回结果
│       │   └── util/Calculator.java
│       ├── main/resources/META-INF/spring
│       │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│       └── test/java/com/taoqi/common
│           ├── aspect/
│           ├── autoconfigure/
│           ├── result/
│           └── util/CalculatorTest.java
└── taoqi-agent-integration     # 第三方服务接入模块（聚合 pom）
    ├── pom.xml
    ├── taoqi-agent-integration-api    # 接口契约（Feign + DTO + 实体）
    │   └── src
    │       ├── main/java/com/taoqi/integration
    │       │   ├── api/            # EmailApi 接口契约与 DTO
    │       │   ├── client/         # EmailFeignClient
    │       │   ├── entity/         # EmailSendRecordEntity 实体
    │       │   └── enums/          # EmailSendStatus 发送状态
    │       └── test/java/com/taoqi/integration/client
    └── taoqi-agent-integration-server # 服务实现（可独立启动）
        └── src
            ├── main/java/com/taoqi/integration
            │   ├── IntegrationApplication.java
            │   ├── config/         # 配置属性
            │   ├── controller/     # REST 接口（邮件发送、记录查询）
            │   ├── event/          # EmailSendEvent 与异步监听器
            │   ├── exception/      # 全局异常处理
            │   ├── mapper/         # EmailSendRecordMapper
            │   ├── service/        # 邮件提交与记录状态维护
            │   └── task/           # EmailSendTask 异步发送任务
            ├── main/resources
            │   ├── application.yml
            │   ├── db/schema.sql   # MySQL 建库建表脚本
            │   └── mapper/         # EmailSendRecordMapper.xml
            └── test/java/com/taoqi/integration
└── taoqi-agent-chat            # 聊天模块（聚合 pom）
    ├── pom.xml
    ├── taoqi-agent-chat-api    # 接口契约（暂未定义）
    └── taoqi-agent-chat-server # 服务实现（Feign 调用示例）
        └── src
            ├── main/java/com/taoqi/chat
            │   ├── ChatApplication.java
            │   ├── cache/          # VerificationCodeStore：验证码 Redis / 内存存储
            │   ├── config/         # 配置属性
            │   ├── constant/       # 邮件 HTML 模板与验证码常量
            │   ├── controller/     # 测试接口、验证码接口
            │   ├── dto/            # 请求参数
            │   ├── facade/         # 门面层：封装 Feign 调用
            │   ├── handler/        # 全局异常处理
            │   └── service/        # 验证码服务
            ├── main/resources/application.yml
            └── test/java/com/taoqi/chat
└── taoqi-agent-starter         # 自定义 Starter（自动配置）
    └── src
        ├── main/java/com/taoqi/starter/accesslog
        │   ├── AccessLogProperties.java            # 配置属性
        │   ├── TaoqiAccessLogAutoConfiguration.java # 自动配置入口
        │   └── TraceIdFilter.java                  # traceId 与访问日志过滤器
        ├── main/resources/META-INF/spring
        │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
        └── test/java/com/taoqi/starter/accesslog
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
curl -X POST http://localhost:8081/api/integration/email/send \
  -H "Content-Type: application/json" \
  -d '{
        "to": ["someone@example.com"],
        "subject": "测试邮件",
        "content": "<h1>你好</h1>",
        "html": true
      }'
```

接口是异步的：收到请求后立即落一条状态为 `SENDING` 的记录并返回记录 ID，SMTP 发送在独立线程池中完成。返回示例（所有接口统一使用 `R` 包装）：

```json
{
  "code": 200,
  "message": "success",
  "data": {"recordId": 1, "status": "SENDING", "message": "邮件已提交，正在发送"},
  "success": true
}
```

发送结果通过 `GET /api/integration/email/records/{id}` 查询，状态会更新为 `SUCCESS` 或 `FAILED`。

### Feign 客户端

其它服务引入 `taoqi-agent-integration-api` 依赖后，在启动类或配置类上添加：

```java
@EnableFeignClients(basePackages = "com.taoqi")
```

各服务的 Feign 客户端统一放在 `com.taoqi.<domain>.client` 包下，扫描根包即可覆盖，新增服务无需修改启动类。

随后注入 `EmailFeignClient` 即可调用邮件服务，返回类型是统一的 `R<EmailSendResult>`：

```java
R<EmailSubmitResult> result = emailFeignClient.send(request);
EmailSubmitResult data = result.data();   // recordId + status + message
```

邮件服务启动后会以 `spring.application.name`（`taoqi-agent-integration`）为服务名注册到 Nacos。Feign 客户端不再配置固定 URL，`@FeignClient(name = "taoqi-agent-integration")` 由 Nacos 返回实例列表，再由 Spring Cloud LoadBalancer 选择具体实例。聊天服务因此额外引入了 `spring-cloud-starter-loadbalancer`。

#### 本地 Nacos

两个服务默认连接 `localhost:8848` 的 Nacos 3.x。用 Docker 启动单机模式：

```bash
docker run -d --name nacos \
  -e MODE=standalone \
  -e NACOS_AUTH_ENABLE=false \
  -e NACOS_AUTH_CONSOLE_ENABLE=false \
  -e NACOS_AUTH_TOKEN="$(printf '%s' 'taoqi-agent-nacos-dev-token-2026-0123456789' | base64)" \
  -p 8080:8080 -p 8848:8848 -p 9848:9848 -p 9849:9849 \
  -v nacos-data:/home/nacos/data \
  nacos/nacos-server:v3.1.1
```

- 控制台 `http://localhost:8080`，服务列表在「服务管理 → 服务列表」；
- 单机模式使用内嵌 Derby，数据保存在 `nacos-data` 卷，不需要外部数据库；
- 应用默认把 `127.0.0.1` 作为注册地址，部署到其它机器时用环境变量 `NACOS_REGISTER_IP` 指定其它服务可达的地址；
- Nacos 地址、命名空间、分组可通过 `NACOS_ADDR`、`NACOS_NAMESPACE`、`NACOS_GROUP` 覆盖。

#### 配置中心

两个服务通过 `spring.config.import` 加载与 `spring.application.name` 同名的 YAML：

```yaml
spring:
  config:
    import: optional:nacos:${spring.application.name}.yml?group=${NACOS_GROUP:DEFAULT_GROUP}&refreshEnabled=true
```

在控制台「配置管理 → 配置列表」创建两个配置，Group 都选 `DEFAULT_GROUP`：

| Data ID | 说明 |
|---|---|
| `taoqi-agent-chat.yml` | 聊天服务配置 |
| `taoqi-agent-integration.yml` | 邮件服务配置 |

示例内容：

```yaml
# taoqi-agent-chat.yml
taoqi:
  chat:
    test-email-to: 1377417617@qq.com
```

```yaml
# taoqi-agent-integration.yml
taoqi:
  mail:
    personal: ChatGPT
```

规则：

- Nacos 配置的优先级高于本地 `application.yml`，同名配置项以 Nacos 为准；
- `optional:` 表示 Nacos 不可用或配置不存在时仍能启动，自动回退到本地配置；
- `refreshEnabled=true` 开启变更监听，需要动态刷新的 `@ConfigurationProperties`（如 `ChatProperties`、`MailProperties`）额外加了 `@RefreshScope`，修改后无需重启即可生效；
- `record` 是 final 类，无法被 Spring Cloud 的 CGLIB 作用域代理继承，所以这两个需要动态刷新的配置类使用可变类 + getter/setter；
- 线程池这类启动期创建的资源不会因配置变更而重建，修改后仍需重启；
- 单元测试通过 `spring.cloud.nacos.config.enabled=false` 和 `import-check.enabled=false` 关闭远端配置。

> 授权码属于敏感信息，生产环境请通过环境变量或配置中心注入，不要提交到代码仓库。

### 邮件发送记录（MyBatis-Plus）

每次调用发信接口都会先写入一条状态为 `SENDING` 的记录并立即返回，SMTP 发送在线程池中异步执行；完成后把状态更新为 `SUCCESS` 或 `FAILED`，并写入 `message_id`、`error_message`、`cost_millis` 和 `sent_at`。

#### 初始化数据库

默认连接 `jdbc:mysql://localhost:3306/taoqi-agent`，用户名 `root`、密码 `123456`，可通过环境变量 `MYSQL_USERNAME` / `MYSQL_PASSWORD` 覆盖。

应用启动时会自动执行 `resources/db/schema.sql` 建表（配置为 `spring.sql.init.mode=always`），脚本使用 `CREATE ... IF NOT EXISTS`，可以重复执行；JDBC URL 带了 `createDatabaseIfNotExist=true`，数据库不存在时先由驱动创建。因此首次启动只需保证 MySQL 已运行、账号有建库建表权限，无需手动执行 SQL。

如果账号没有建库权限，或想手动初始化，也可以直接执行：

```bash
mysql -u root -p < taoqi-agent-integration/taoqi-agent-integration-server/src/main/resources/db/schema.sql
```

脚本会用 `utf8mb4` 创建数据库 `taoqi-agent` 和表 `email_send_record`，并为创建时间、成功状态、收件人建立索引。

#### 数据模型

实体 `EmailSendRecordEntity` 位于 `taoqi-agent-integration-api` 的 `entity` 包，用 `@TableName("email_send_record")` 指定表名、`@TableId(type = IdType.AUTO)` 指定自增主键，字段按下划线规则映射列名。api 模块只引入 `mybatis-plus-annotation`，不引入整个 ORM。

发送状态用枚举 `EmailSendStatus`（`SENDING` / `SUCCESS` / `FAILED`）定义，数据库以枚举名存储，MyBatis 按名称映射。

#### 异步发送

`EmailService.submit` 只做“校验 + 落库 + 发布事件”；`EmailSendListener` 用 `@Async("emailSendExecutor")` + `@EventListener` 消费 `EmailSendEvent`，再调用执行组件 `EmailSendTask` 完成 SMTP 发送并回写状态。执行组件本身不加 `@Async`，职责单一，也保留可以直接调用的测试入口。

发送动作与业务逻辑通过事件解耦：以后要加审计、发送指标、失败重试等能力，只需新增监听器，`EmailService` 不用改。如果给 `submit` 加上事务，可以把监听改成 `@TransactionalEventListener(phase = AFTER_COMMIT)`，确保记录提交后再发送，避免异步线程读到未提交数据。

线程池通过 `AsyncConfig` 注册，参数由 `taoqi.email.async` 配置：

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `taoqi.email.async.core-pool-size` | 2 | 核心线程数 |
| `taoqi.email.async.max-pool-size` | 4 | 最大线程数 |
| `taoqi.email.async.queue-capacity` | 100 | 队列容量 |

线程名前缀 `email-send-`；队列满时使用 `CallerRunsPolicy`（由请求线程执行，形成背压而不是丢任务）；关闭时等待在途任务完成，最多 30 秒，避免记录停留在“发送中”。

**为什么用线程池而不是异步回调**：`JavaMailSender` 是阻塞式 API，没有回调/Future 形式的发送入口，所以“异步回调”在这里没有落点；线程池 + `@Async` 改动最小，还能限制并发连接数、保护 SMTP 服务。

**当前方案的边界**：任务只存在于内存队列，进程重启会丢失未发送的任务，对应记录会一直是 `SENDING`。如果要更强保证，可以加一个定时任务把超时 `SENDING` 记录置为 `FAILED` 或重新投递，或者改用消息队列（RabbitMQ / Kafka）承载发送任务以获得持久化和重试能力。

#### MyBatis-Plus 与 XML 的分工

- **简单操作走 MyBatis-Plus**：`EmailRecordServiceImpl` 继承 `ServiceImpl`，记录写入用 `save`、主键查询用 `getById`、条件分页用 `IService.page` + `LambdaQueryWrapper`（主题模糊、成功状态、时间范围、排序；收件人用 `apply("FIND_IN_SET...")` 精确匹配逗号分隔列表）。分页插件在 `MybatisPlusConfig` 中注册，依赖 `mybatis-plus-jsqlparser`。
- **复杂 SQL 走 XML**：`resources/mapper/EmailSendRecordMapper.xml` 只保留聚合查询：
  - `selectDailyStats`：按天聚合总数、成功数、失败数、平均耗时（`CASE WHEN` + `GROUP BY`）；
  - `selectTopRecipients`：用递归 CTE + `SUBSTRING_INDEX` 把逗号分隔的收件人拆开，统计发送次数排行（MySQL 8+）。

#### 查询接口

```bash
# 分页查询，返回 R<IPage<EmailSendRecordEntity>>，支持 to、subject、status、startTime、endTime、page、size
curl "http://localhost:8081/api/integration/email/records?to=1377417617@qq.com&status=SUCCESS&page=1&size=10"

# 按主键查询单条记录（MyBatis-Plus getById）
curl "http://localhost:8081/api/integration/email/records/1"

# 最近 7 天每日统计
curl "http://localhost:8081/api/integration/email/records/daily-stats?days=7"

# 收件人 Top 5
curl "http://localhost:8081/api/integration/email/records/top-recipients?limit=5"
```

> 单元测试使用 H2 内存库验证实体映射与基础 CRUD；复杂 SQL 依赖 MySQL 8 的递归 CTE 与字符串函数，需要在本地数据库上验证。

## 聊天模块

`taoqi-agent-chat` 与 `taoqi-agent-common` 平级，同样按“聚合模块 + 接口/实现分离”组织：

- `taoqi-agent-chat-api`：聊天模块的接口契约（暂未定义，先保留模块结构）
- `taoqi-agent-chat-server`：聊天模块的服务实现，注册到 Nacos，通过 Feign 调用邮件服务，默认端口 8082

调用链：`ChatController` → `EmailClientFacade` → `EmailFeignClient` → Nacos 服务发现 → 邮件服务。

Facade 按被调用的服务命名（邮件服务对应 `EmailClientFacade`），职责是拆包 client 返回的 `R`，向上只暴露业务数据；controller 再统一用 `R` 包装响应。

聊天服务同时引入了 `taoqi-agent-starter`，请求会自动带上 traceId 并打印访问日志；并使用了 common 模块的 `@RateLimit` 注解限制接口调用频率。此外提供一个连通性接口 `GET /api/chat/ping`（每来源 30 秒内最多 2 次），便于验证限流。详见后文。

### Feign 调用测试

先启动 Nacos（见上文），再启动邮件服务（8081），最后启动聊天服务（8082）：

```bash
./mvnw -pl taoqi-agent-integration/taoqi-agent-integration-server spring-boot:run
./mvnw -pl taoqi-agent-chat/taoqi-agent-chat-server spring-boot:run
```

调用测试接口，不传 `to` 时使用 `taoqi.chat.test-email-to` 配置的默认收件人：

```bash
curl "http://localhost:8082/api/chat/email/test?to=someone@example.com"
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {"recordId": 1, "status": "SENDING", "message": "邮件已提交，正在发送"},
  "success": true
}
```

邮件是异步发送的，最终结果要到邮件服务的 `/api/integration/email/records/{id}` 查询。

### 邮箱验证码

`POST /api/chat/verification-code` 生成 6 位验证码，写入 Redis（5 分钟过期），并通过 HTML 模板发送验证码邮件。模板在 `EmailTemplateConstants`，风格参考 ChatGPT 的验证邮件；占位符使用 `{{code}}`、`{{expireMinutes}}`，避免和 CSS 中的百分号冲突。

```bash
curl -X POST http://localhost:8082/api/chat/verification-code \
  -H "Content-Type: application/json" \
  -d '{"email":"someone@example.com"}'
```

返回示例：

```json
{"code":200,"message":"success","data":{"recordId":3,"status":"SENDING","message":"邮件已提交，正在发送"},"success":true}
```

校验验证码：

```bash
curl -X POST http://localhost:8082/api/chat/verification-code/verify \
  -H "Content-Type: application/json" \
  -d '{"email":"someone@example.com","code":"123456"}'
```

校验成功返回 `{"code":200,"message":"success","data":null,"success":true}`；验证码错误或已过期返回业务码 400：

```json
{"code":400,"message":"验证码错误或已过期","data":null,"success":false}
```

说明：

- 发送接口每个来源每分钟最多调用 3 次，校验接口每分钟最多尝试 5 次；邮箱为空或格式错误、验证码不是 6 位数字时返回 400；
- 验证码保存在 `VerificationCodeStore`，默认 Redis 实现：key 为 `taoqi:chat:verification-code:{小写邮箱}`，TTL 5 分钟；校验用 Lua 脚本原子比较并删除，只有校验成功才失效，输错验证码不会让正确验证码被消费；同一邮箱重新发送会覆盖旧码；
- 单实例或测试环境可通过 `taoqi.chat.verification-code.store-type=memory` 切到 Caffeine 内存实现（环境变量 `VERIFICATION_CODE_STORE_TYPE`）；
- Redis 不可用时发码接口直接失败（fail-closed），不会发出一封无法校验的邮件；
- 校验失败的 HTTP 状态码仍是 200，业务结果看 `R.code`；参数格式错误由全局异常处理返回 HTTP 400。

## 自定义 Starter：taoqi-agent-starter

`taoqi-agent-starter` 演示 Spring Boot Starter 的完整实现方式：使用方只需引入依赖，不写任何 `@EnableXxx` 注解、不做额外配置，功能就自动生效。

### 功能

自动为 Servlet Web 应用加上请求级 traceId 和访问日志：

- 请求头 `X-Trace-Id` 有值时直接复用（便于跨服务串联），没有则生成 16 位 ID；
- traceId 回写到响应头，同时写入 MDC，日志格式里用 `%X{traceId}` 即可打印；
- 请求结束后打印方法、路径、状态码与耗时，超过阈值打印 WARN 慢请求日志；
- 支持整体关闭、自定义头名 / MDC 键、排除路径。

### 实现构成

| 文件 | 作用 |
|---|---|
| `TaoqiAccessLogAutoConfiguration` | 自动配置入口，用 `@AutoConfiguration` + 条件注解声明何时生效 |
| `AccessLogProperties` | `@ConfigurationProperties(prefix = "taoqi.access-log")`，绑定配置并生成 IDE 元数据 |
| `TraceIdFilter` | `OncePerRequestFilter`，真正干活的过滤器 |
| `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | 告诉 Spring Boot 启动时加载哪个自动配置类 |

关键点：

1. **注册方式**：Boot 3 起不再用 `spring.factories`，自动配置类的全限定名写进 `AutoConfiguration.imports` 文件，启动时由 `@EnableAutoConfiguration`（`@SpringBootApplication` 已包含）读取。自动配置类不需要落在使用方的组件扫描路径下。
2. **条件装配**：`@ConditionalOnWebApplication` 限制只在 Servlet 应用生效，`@ConditionalOnProperty` 提供开关，`@ConditionalOnClass` 在类路径缺少 Web 相关类时安全退出。
3. **自动退让**：`@ConditionalOnMissingBean` 让使用方可以自己声明 `TraceIdFilter`，覆盖默认实现。
4. **依赖 optional**：过滤器依赖 Web 环境，starter 中把 `spring-boot-starter-webmvc` 声明为 `optional`，非 Web 应用引入时不会被拖入 Tomcat。
5. **配置元数据**：`spring-boot-configuration-processor` 在编译期生成 `spring-configuration-metadata.json`，IDE 里输入 `taoqi.access-log.` 会有配置提示。

### 配置项

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `taoqi.access-log.enabled` | `true` | 是否启用 |
| `taoqi.access-log.header-name` | `X-Trace-Id` | traceId 头名称 |
| `taoqi.access-log.mdc-key` | `traceId` | MDC 键名 |
| `taoqi.access-log.slow-threshold` | `1s` | 慢请求阈值 |
| `taoqi.access-log.exclude-path-patterns` | 空 | 排除路径，Ant 风格，如 `/actuator/**` |

### 使用

其它模块引入依赖即可，版本由父模块统一管理：

```xml
<dependency>
    <groupId>com.taoqi</groupId>
    <artifactId>taoqi-agent-starter</artifactId>
</dependency>
```

要在日志中看到 traceId，需要在自己的日志格式里引用 MDC，例如 `taoqi-agent-chat-server` 的 `application.yml`：

```yaml
logging:
  pattern:
    console: "%d{HH:mm:ss.SSS} %-5level [%X{traceId:-}] %logger{36} - %msg%n"
```

验证：启动聊天服务后请求任意地址（404 也会被记录）：

```bash
curl -i http://localhost:8082/api/chat/ping
# 响应头包含 X-Trace-Id: 44859a7e59c944c3

curl -i -H "X-Trace-Id: demo-123" http://localhost:8082/api/chat/ping
# 响应头回显 X-Trace-Id: demo-123
```

日志输出：

```
15:46:17.011 INFO  [44859a7e59c944c3] c.t.starter.accesslog.TraceIdFilter - GET /api/chat/ping -> 404，耗时 9 ms，traceId=44859a7e59c944c3
```

> 命名约定：社区习惯把 starter 命名为 `xxx-spring-boot-starter`，自动配置单独拆成 `xxx-spring-boot-autoconfigure`；当 starter 依赖较多 optional 组件时才需要拆分，像本模块这样只有一个可选 Web 依赖时，单模块更直观。

## 公共能力：@RateLimit 限流注解

`taoqi-agent-common` 提供 `@RateLimit` 注解，用于限制方法（通常是 HTTP 接口）的调用频率，防止接口被刷、重复发信等。common 模块内置自动配置，使用方引入依赖后加注解即可。

### 用法

```java
@RateLimit(limit = 5, windowSeconds = 60)
@GetMapping("/api/chat/email/test")
public EmailSendResult sendTestEmail(...) { ... }
```

| 参数 | 默认值 | 说明 |
|---|---|---|
| `limit` | 10 | 时间窗口内允许的最大调用次数 |
| `windowSeconds` | 60 | 时间窗口长度（秒） |
| `key` | 空 | 自定义限流 key 前缀，留空时使用方法签名 |

### 实现

| 文件 | 作用 |
|---|---|
| `annotation/RateLimit` | 注解定义 |
| `aspect/RateLimitAspect` | AOP 切面：拼 key 并委托给 `RateLimitStore` |
| `ratelimit/RateLimitStore` | 限流计数存储接口 |
| `ratelimit/InMemoryRateLimitStore` | 内存实现（固定窗口，单实例） |
| `ratelimit/RedisRateLimitStore` | Redis 实现（Lua 原子计数，多实例共享） |
| `ratelimit/RateLimitProperties` | `taoqi.rate-limit.*` 配置 |
| `exception/RateLimitExceededException` | 超限异常，携带建议重试秒数 |
| `autoconfigure/RateLimitAutoConfiguration` | 自动注册切面与存储实现 |

规则：

- 限流 key = 方法签名（或注解 `key`）+ 调用方 IP，同一接口不同客户端互不影响；有 `X-Forwarded-For` 时优先取它。
- 固定窗口计数：窗口期内累加，窗口过期后从 1 重新开始。
- 超限抛出 `RateLimitExceededException`；聊天服务的 `GlobalExceptionHandler` 将其转换为 `429 Too Many Requests`，并通过 `Retry-After` 返回建议重试秒数。
- 存储实现通过配置切换，默认内存；多实例部署切换到 Redis。Redis 不可用时采取放行策略（fail-open），避免限流组件拖垮业务。
- 固定窗口在边界可能出现瞬时双倍流量，要求更平滑时可换成令牌桶或滑动窗口。

### 存储切换

| 配置 | 默认值 | 说明 |
|---|---|---|
| `taoqi.rate-limit.store-type` | `memory` | `memory` 单机内存；`redis` 多实例共享 |

Redis 模式需要在运行期引入 `spring-boot-starter-data-redis`（chat-server 已引入），并配置连接：

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

taoqi:
  rate-limit:
    store-type: redis
```

Redis 实现用 Lua 脚本原子完成“自增 + 首次设置过期 + 读取剩余 TTL”，key 形如 `方法签名#客户端IP`，所有实例共享同一份额度。

### 验证

```bash
curl -i http://localhost:8082/api/chat/ping   # 第 1、2 次：200，返回 R 结构
curl -i http://localhost:8082/api/chat/ping   # 第 3 次：429，Retry-After: 30
```

## 公共能力：统一返回结果 R

`taoqi-agent-common` 的 `R<T>` 用于统一 HTTP 接口返回结构，字段固定为 `code`、`message`、`data`、`success`。`data` 是泛型，单个对象、列表、分页数据都能直接放，例如 `R<List<User>>`。

项目所有 REST 接口（含 Feign 契约）的响应体都使用 `R`；分页接口直接使用 `R<IPage<T>>`，把 MyBatis-Plus 查出的分页对象原样作为 `data` 返回。

### 静态方法

| 方法 | 说明 |
|---|---|
| `R.data(data)` | 成功并携带数据 |
| `R.list(list)` | 成功并携带列表数据（等价于 `R.data(list)`，语义更明确） |
| `R.ok()` | 成功且无数据，返回 `R<Void>` |
| `R.success(message, data)` | 成功并自定义提示信息 |
| `R.fail(message)` | 失败，状态码默认 `R.FAIL_CODE`（500） |
| `R.fail(code, message)` | 失败并指定业务状态码 |
| `R.fail(code, message, data)` | 失败并携带补充数据 |
| `R.of(code, message, data, success)` | 完全自定义 |

### 使用

```java
R<User> user = R.data(user);
R<List<User>> users = R.list(userList);
R<IPage<User>> page = R.data(pageResult);   // 直接放 MyBatis-Plus 的分页对象
R<Void> failed = R.fail("参数错误");
```

聊天服务的 `GET /api/chat/ping` 已改为返回 `R<String>`，响应示例：

```json
{"code":200,"message":"success","data":"pong","success":true}
```

## 开发约定

- **各服务独立版本号**：根目录下每个服务模块在自身 `pom.xml` 中声明自己的 `<version>`，根 `pom.xml` 中用同名独立属性（`taoqi-agent-common.version`、`taoqi-agent-integration.version`、`taoqi-agent-chat.version`、`taoqi-agent-starter.version`）供 `dependencyManagement` 引用。升级某个服务时只改它自己的版本号，不影响其它服务；服务下的 `-api` / `-server` 跟随所属服务的版本。
- 升级带子模块的服务（如 `taoqi-agent-integration`）时，需同步修改：服务自身 `<version>`、其子模块 `parent` 中的 `<version>`、根 pom 中对应的版本属性。
- 第三方依赖版本统一在父模块 `pom.xml` 的 `dependencyManagement` 中管理，子模块引用依赖时不写 `<version>`；内部服务之间的依赖也由父模块按各自版本属性统一管理。
- 新增依赖时，版本属性（如 `<xxx.version>`）和 BOM / 依赖条目写在父模块。
- 服务类模块的拆分方式：与 `taoqi-agent-common` 平级的聚合模块（如 `taoqi-agent-integration`）下再建 `-api`（接口契约、Feign 客户端、DTO）和 `-server`（服务实现）两个子模块，其它服务只需引入 `-api` 模块。
- 源码与构建统一使用 UTF-8 编码。

## 后续待办

- [ ] **接入 Spring Cloud Gateway**（模块名 `taoqi-agent-gateway`）：新增网关服务，按 `/api/chat/**` → `lb://taoqi-agent-chat`、`/api/integration/**` → `lb://taoqi-agent-integration` 配置路由，由 Nacos 服务发现 + LoadBalancer 自动选择实例。
  - 接入后 nginx 不再维护静态 upstream 列表，只保留静态资源与 TLS 终止；服务扩缩容不需要修改 nginx 配置。
  - 网关自身需要注册到 Nacos，并引入 `spring-cloud-starter-gateway`、`spring-cloud-starter-alibaba-nacos-discovery`、`spring-cloud-starter-loadbalancer`。
