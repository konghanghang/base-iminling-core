# 从 2.x 迁移到 3.x

[返回 README](../README.md) · [功能与用法](usage.md) · [配置参考](configuration.md) · [可选集成](integrations.md)

本指南面向引用旧版 `com.iminling` 公共库的项目。3.x 是新的兼容性基线，需要重新编译和接入验证，不保证与旧版二进制兼容。

## 1. 确认目标运行环境

| 项目 | 3.x 基线 |
| --- | --- |
| Java | 最低 17，编译目标 17 |
| Spring Boot / Spring Framework | 4.0.9 / 7.0.x |
| Spring Cloud | 使用 Feign 时配套 2025.1.4 |
| JSON | Jackson 3 |
| 本库版本 | `3.0.0-SNAPSHOT` |
| 构建 | Maven 3.9.11（通过 Wrapper），移除 Gradle |

Spring Boot 2 / Java 8 项目不能只替换公共库版本。先确定业务应用的框架升级范围，再替换依赖。需要维持旧运行环境时继续使用旧版库。

本仓库的 Kotlin 源码和测试已转为 Java，消费项目无需 Kotlin 编译配置；OkHttp 传递引入的 Kotlin 标准库仍存在，不等同于运行时依赖完全没有 Kotlin。

## 2. 本地安装并替换依赖

当前版本尚未发布到 Maven 仓库，先在本仓库构建并安装：

```sh
./mvnw clean verify
./mvnw install
```

在消费项目更新公共库版本，并启用方法参数名：

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.release>17</maven.compiler.release>
    <maven.compiler.parameters>true</maven.compiler.parameters>
</properties>

<dependencies>
    <dependency>
        <groupId>com.iminling</groupId>
        <artifactId>iminling-core</artifactId>
        <version>3.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

仅使用工具类的项目可以引用 `iminling-common`。业务项目不需要改成与本库相同的构建工具，但应配置等价的 Java 版本和 `-parameters`。

自动配置入口已改用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，本库不再通过旧 spring.factories 注册这组自动配置。正常的 Spring Boot 应用不需要为此扫描 `com.iminling`。

## 3. 更新 Java 与 JSON API 调用

### Java 静态方法

旧版 Java 调用中的 `.Companion` 改为静态方法调用，例如 `AESUtils.Companion.encryptECB(...)` 改为 `AESUtils.encryptECB(...)`。AES 相关方法声明 GeneralSecurityException，需要在调用处捕获或透传；证书 HTTP 方法还可能声明 IOException。

过滤器接口的 getAnnotation 是 Java 默认方法，业务实现无需补写。可变模型仍通过 Java getter / setter 访问；不要把此次迁移理解为所有模型均已改成不可变 record。

### Jakarta 包名

| 旧 API | 新 API |
| --- | --- |
| `javax.servlet.*` | `jakarta.servlet.*` |
| `javax.validation.*` | `jakarta.validation.*` |

JDK 自带的 `javax.crypto`、`javax.net.ssl`、`javax.imageio` 保持原包名。`org.hibernate.validator.*` 属于仍在使用的校验实现，不是本库继续集成了 JPA / Hibernate ORM。

### Jackson 3

| 旧 API | 3.x 中的 API |
| --- | --- |
| `com.fasterxml.jackson.core.*` | `tools.jackson.core.*` |
| `com.fasterxml.jackson.databind.*` | `tools.jackson.databind.*` |
| `com.fasterxml.jackson.dataformat.*` | `tools.jackson.dataformat.*` |
| 通用 Jackson 注解 | 仍为 `com.fasterxml.jackson.annotation.*` |

Jackson 3 还更改了部分类型，例如 ValueSerializer、ValueDeserializer、SerializationContext；不要仅批量替换包名前缀。Java 时间支持已集成，不再照搬旧版的 jsr310 module 配置。

JsonUtil.getInstant() 现在返回 JsonMapper；JsonUtil 的 TypeReference 重载使用 Jackson 3 类型。定制 mapper 使用 builder，见[配置参考](configuration.md#定制-jsonmapper)。

保留的默认 JSON 协议包括 Long 转字符串和固定日期时间格式。迁移业务应用后，仍需检查自定义 JsonMapper、消息转换器、前端类型和 Feign 返回类型是否遵守同一约定。

## 4. 调整可选组件与已移除能力

### MyBatis-Plus 与 Feign

这些依赖是 optional，需由消费项目显式声明，完整片段见[可选集成](integrations.md)。

- MyBatis-Plus 使用 Boot 4 starter 和单独的 jsqlparser 模块，默认分页为 MySQL。
- Feign 使用 Spring Cloud LoadBalancer 替代 Ribbon，依赖 OpenFeign、LoadBalancer 和 feign-okhttp。
- 本库不再固定旧 Spring Cloud Alibaba BOM，也没有 Nacos 专用自动配置，业务按目标兼容版本接入服务发现。
- 自定义 Client、Decoder、MybatisPlusInterceptor 或 MetaObjectHandler 时，应检查默认 Bean 是否退让，以及业务实现是否保留所需能力。

### JPA / QueryDSL

本库已移除 JPA 自动配置、QueryDSL 依赖、JPageBuilder，以及相应测试和注册项。原先依赖本库提供 JPAQueryFactory 或分页构建的代码，需要在业务项目自行实现，或改用仍保留的 MyBatis-Plus 分页。

### Swagger / OpenAPI

本库已移除 Swagger/OpenAPI 依赖、UI、文档生成扩展、SwaggerDisplayEnum 注解和 Knife4jApiInfoProperties。旧的 `knife4j.info.*` 配置不再由本库读取，也不再提供文档端点。

需要 API 文档的业务项目自行引入兼容目标 Spring Boot 的文档组件和注解；原来自定义多参数 JSON、返回包装和枚举展示的文档扩展不会自动恢复。

本次删除作用于当前版本，没有改写 Git 历史或清除旧提交中的文件。

## 5. 检查接入行为

按实际使用的能力验证：

- `@EnableResolve` 的策略、嵌套 JSON 参数名、空值及 Optional；消费项目已启用 `-parameters`。
- 统一返回的结构、Long 字符串、日期格式；ResponseEntity、流式响应和直接 ResultModel 的处理方式。
- HTTP 状态码与业务 code；参数校验、BizException 当前通常使用 HTTP 500。
- 业务 LoginFilter / AuthFilter 的注册、注解判断、组内顺序和请求上下文。路径排除同时跳过本库 MVC 拦截器，见[日志说明](usage.md#日志与路径排除)。
- core 默认 CORS、内容协商和消息转换设置与业务原有 MVC 配置是否一致，见[配置参考](configuration.md#默认-web-行为)。
- MyBatis 真实查询、分页、填充和现有 customizer；Feign 服务发现、返回解包、超时和业务失败码处理。

`filters.log` 没有实现配置开关；按 URL 设置 RestTemplate 超时仍是固定 `/index` 规则。迁移时不要把旧常量或示例类当作已经外部化的配置能力。

## 验证入口

```sh
./mvnw clean verify
./mvnw install
./mvnw -f integration-tests/consumer/pom.xml clean verify
```

仓库测试覆盖 JSON/XML、AES、MVC 参数与返回、校验、鉴权失败、请求上下文清理、Feign 解包、本地 HTTP、H2 下的 MyBatis 查询和 mapper 重载。独立消费项目验证真实 JAR 的自动配置与 HTTP 行为，并断言 JPA、QueryDSL、Swagger/OpenAPI 不在类路径中。

本地验证使用本机 JDK 17，不要求安装其他 JDK。真实注册中心、生产数据库、远端服务和 Maven Central 发布凭据需在相应环境验证；GitHub 上的代码推送不代表 Maven 制品已经发布。
