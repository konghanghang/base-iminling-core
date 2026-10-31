# base-iminling-core

Java 公共工具和 Spring Boot 自动配置库。`3.x` 使用 Maven 和纯 Java 源码，最低支持 **Java 17**，面向 **Spring Boot 4.0.x**；旧版 `2.x` 的 Spring Boot 2 / Java 8 项目需要迁移后才能使用。

## 版本与构建

| 项目 | 当前基线 |
| --- | --- |
| Java | 最低 17；使用较新 JDK 构建时仍以 `--release 17` 编译 |
| Maven | Wrapper 固定 3.9.11；直接使用 Maven 时要求 3.9+ |
| 本库 | `3.0.0-SNAPSHOT` |
| Spring Boot / Spring Framework | 4.0.9 / 7.0.x |
| Spring Cloud | 2025.1.4，与 Boot 4.0.9 配套 |
| JSON | Jackson 3 |

```sh
./mvnw clean verify
./mvnw install
# 用已安装的真实 JAR 验证独立的最小 Spring Boot 消费项目
./mvnw -f integration-tests/consumer/pom.xml verify
```

Windows 使用 `mvnw.cmd`。首次运行 Wrapper 需要下载 Maven 和依赖。构建只需要本机 JDK 17+，普通测试和打包不需要签名密钥、发布凭据、数据库或外部服务；数据库测试使用 H2。Telegram 发消息测试默认禁用。

## 模块与引用

- `iminling-common`：日期、文件、缓存、加解密、JSON/XML、OkHttp 等工具。
- `iminling-core`：MVC 参数与统一返回、异常处理、过滤器、请求日志、参数校验，以及可选的 MyBatis-Plus 和 Feign 集成。

本版本尚未发布，先在仓库运行 `./mvnw install`，再在消费项目引用：

```xml
<dependency>
    <groupId>com.iminling</groupId>
    <artifactId>iminling-core</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

只需要工具类时可单独引用 `iminling-common`。自动配置通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 加载，不需要扫描 `com.iminling` 包。

源码和构建均不再使用 Kotlin 编译器、插件或 DSL。OkHttp 自身仍会传递引入 Kotlin 标准库，使用本库不需要配置 Kotlin 编译。

## 自定义参数与统一返回

`@EnableResolve` 默认只包装返回值；使用 `ARGUMENTS` 只处理 JSON 参数，`ALL` 同时处理参数与返回值，`NONE` 关闭两者。

```java
@PostMapping("/list")
@EnableResolve(ResolveStrategy.ALL)
public PageResp<Item> list(Condition condition, PageModel pageModel) {
    // ...
}
```

对应 JSON 请求：

```json
{
  "condition": {"name": "example"},
  "pageModel": {"pageNum": 1, "pageSize": 10}
}
```

无需给上述参数添加 `@RequestBody`。编译消费项目时应启用 `-parameters`（Maven `maven.compiler.parameters=true`），参数名用于匹配 JSON 字段。`@PathVariable`、`@RequestHeader` 和 Servlet 请求/响应参数保留 Spring 的处理方式。

统一返回结构：

```json
{"code": 200, "data": {}, "message": "请求成功"}
```

已经返回 `ResultModel`、`ResponseEntity` 或流式响应的方法不重复包装。原有 JSON 约定保留：Long 输出为字符串，日期时间默认 `yyyy-MM-dd HH:mm:ss`；Jackson 3 的 mapper 不再支持原地修改，应通过 builder 定制。

## 过滤器、日志和校验

实现 `com.iminling.core.config.filter.LoginFilter` / `AuthFilter`，并注册为 Spring Bean。通过 `getOrder()` 控制顺序，`getAnnotation()` 是 Java 默认方法，可识别组合注解。`@LoginRequired` 标记登录要求，`@Authentication` 提供角色和权限信息，具体鉴权逻辑由业务过滤器实现。

- `filters.login=disable` / `filters.auth=disable`：关闭对应类型的业务过滤器，默认启用。
- `ThreadContext` 保存当前请求的 `ClientInfo`、用户和属性，请求结束后清理。
- HTTP 请求/响应日志由 `CustomizeGlobalFilter` 记录，排除静态资源等路径。
- 在 Controller 方法上添加 `@Validate`，配合 `jakarta.validation` 注解和 `com.iminling.core.validation.group` 中的分组执行校验。

定时任务错误处理仍支持 `errorHandler.enable`、`error.notice.user` 和自定义 `SendNoticeService`；已有业务 `TaskScheduler` 时不会覆盖它。

## 可选组件

这些组件在本库 POM 中标记为 `optional`，不会自动传播到消费项目。业务按需引入并配置；没有这些组件时，基础 MVC 自动配置仍可启动。

| 能力 | 业务项目需要的依赖 |
| --- | --- |
| MyBatis-Plus | `com.baomidou:mybatis-plus-spring-boot4-starter:3.5.16` 和 `mybatis-plus-jsqlparser:3.5.16` |
| Feign 与负载均衡 | `spring-cloud-starter-openfeign`、`spring-cloud-starter-loadbalancer` 和 `io.github.openfeign:feign-okhttp`；导入 Spring Cloud 2025.1.4 BOM |
| Nacos | 在消费项目按目标版本引入相应组件和 BOM；本库不再固定旧 Spring Cloud Alibaba BOM |

MyBatis-Plus 集成包括分页、`createTime` / `updateTime` 填充、Map 键的下划线转驼峰，以及默认开启的 SQL 日志（`mybatis.sql.log=false` 关闭）。XML 热加载默认关闭，可在开发环境设置 `mybatis-plus.refresh-mapper=true` 和 `mybatis-plus.refresh-mapper-interval=5`（秒，必须大于 0）；该功能依赖 MyBatis 内部结构，升级 MyBatis 后需要重新验证。

Feign 使用 Spring Cloud LoadBalancer 替代 Ribbon；自定义 Client 与 Decoder 默认启用，设置 `app.customize.feign=false` 可关闭。声明返回 `ResultModel<T>` 时保留完整结构，声明返回 `T` 时提取响应的 `data`；普通 JSON 响应交回标准 Decoder。`@FeignClient` 接口不会被当作 MVC Controller 注册。

## 从 2.x 迁移

1. 消费项目升级到 Java 17+ 和匹配的 Spring Boot / Spring Cloud 版本。
2. 将 Servlet、Validation 的 `javax.*` 引用改为对应 `jakarta.*`。JDK 自带的 `javax.crypto`、`javax.net.ssl`、`javax.imageio` 等保持不变。
3. Java 调用工具类时直接使用静态方法，例如 `AESUtils.encryptECB(...)`，删除旧的 `.Companion`；Java 加解密方法声明的受检异常需要捕获或透传。
4. 直接使用 JSON API 的代码从 Jackson 2 的 core/databind 包迁到 `tools.jackson.*`；通用 Jackson 注解仍是 `com.fasterxml.jackson.annotation.*`。
5. JPA/QueryDSL 和 Swagger/OpenAPI 集成已移除；有这些需求的消费项目自行配置。按需重新引入本库仍支持的可选组件。
6. 这是新的大版本，不保证旧二进制兼容；旧调用方需要重新编译并验证。

## 发布准备

普通构建会生成 JAR、POM、源码包、Javadoc 包和 `git.properties`。`central` profile 提供 GPG 签名与 Central Publishing Portal 插件，替代已经淘汰的 OSSRH staging 配置。

正式发布前需要设置正式版本、准备 GPG 密钥和 Maven settings 中 id 为 `central` 的发布凭据，再显式运行发布命令。`autoPublish=false`；本次迁移不执行远程发布，也没有验证远程凭据和仓库权限。
