# base-iminling-core

Java 公共工具和 Spring Boot 自动配置库，提供 JSON/XML、HTTP、缓存、加解密工具，以及 MVC 参数解析、统一返回、异常处理、校验和业务过滤器扩展。MyBatis-Plus、Feign 按需接入。

## 版本要求

| 项目 | 当前基线 |
| --- | --- |
| 本库 | `3.0.0-SNAPSHOT`，尚未发布到 Maven 仓库 |
| Java | 最低 17；编译目标固定为 `--release 17` |
| Spring Boot / Spring Framework | 4.0.9 / 7.0.x |
| Spring Cloud | 接入 Feign 时使用 2025.1.4，与 Boot 4.0.9 配套 |
| JSON | Jackson 3 |
| Maven | Wrapper 固定 Maven 3.9.11；直接使用 Maven 时要求 3.9+ |

`3.x` 为纯 Java 源码和 Maven 构建。OkHttp 会传递引入 Kotlin 标准库，消费项目不需要配置 Kotlin 编译。JPA/QueryDSL 和 Swagger/OpenAPI 集成已移除，旧项目请先阅读[迁移指南](docs/migration-java17.md)。

## 选择模块

| 模块 | 用途 | 是否自动配置 Spring Boot |
| --- | --- | --- |
| `iminling-common` | 日期、文件、JSON/XML、OkHttp、缓存、加解密和 ID 工具 | 否，按需调用工具类 |
| `iminling-core` | MVC 参数与返回、异常、校验、请求日志、业务过滤器、定时任务错误通知；可选 MyBatis-Plus / Feign 集成 | 是，同时依赖 common |

## 快速接入

### 1. 安装当前版本

在本仓库使用本机 JDK 17 构建并安装到本地 Maven 仓库：

```sh
./mvnw clean verify
./mvnw install
```

Windows 使用 `mvnw.cmd`。首次运行需要下载 Maven 和依赖，普通构建不需要签名或发布凭据。数据库测试使用 H2；Telegram 真实发送测试默认禁用。

### 2. 添加依赖

以下配置加入一个使用 Spring Boot 4.0.9 的消费项目。示例中的依赖版本目前需要先完成上面的本地安装。

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

只需要工具类时，将 `artifactId` 改为 `iminling-common`。core 通过 Spring Boot 的自动配置清单加载，无需扫描 `com.iminling` 包。参数名用于匹配 JSON 字段，因此消费项目必须保留 `-parameters`。

### 3. 添加一个接口

下面是可直接放入消费项目的 `ExampleApplication.java`：

```java
package example;

import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.annotation.Validate;
import com.iminling.core.constant.ResolveStrategy;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    public record Input(@NotBlank String name) {}
    public record Greeting(String text, long id) {}

    @PostMapping("/greetings")
    @EnableResolve(ResolveStrategy.ALL)
    @Validate
    public Greeting greet(Input input) {
        return new Greeting("Hello, " + input.name(), 1L);
    }
}
```

启动消费项目后请求接口：

```sh
curl -X POST http://localhost:8080/greetings \
  -H 'Content-Type: application/json' \
  -d '{"input":{"name":"Alice"}}'
```

返回结构：

```json
{"code":200,"data":{"text":"Hello, Alice","id":"1"},"message":"请求成功"}
```

这里的 `input` 来自方法参数名，`id` 按本库 JSON 约定输出为字符串。完整的独立消费项目示例见 [integration-tests/consumer](integration-tests/consumer)。

## 接入后需要了解的默认行为

- core 自动注册请求日志、异常处理、校验切面、参数处理器，以及默认 JsonMapper、RestTemplate 和 MVC 配置。
- 自定义参数解析和统一返回由 `@EnableResolve` 控制；默认策略只处理返回值。
- 登录与权限注解提供标记，业务必须实现并注册 `LoginFilter` / `AuthFilter`，才能执行认证与授权。
- 默认 JSON 将 Long 输出为字符串，日期时间格式为 `yyyy-MM-dd HH:mm:ss`。
- 默认 MVC 配置忽略 `Accept` 请求头，以 JSON 作为默认响应类型，并注册允许任意 Origin、请求头和方法的 CORS 过滤器。
- 参数校验和一般业务异常当前通常返回 HTTP 500；业务 `code` 与 HTTP 状态码需要分别处理。

具体生效条件和覆盖方式见[配置参考](docs/configuration.md)。

## 文档导航

| 文档 | 内容 |
| --- | --- |
| [功能与用法](docs/usage.md) | 参数与返回、异常、过滤器、校验、日志、请求上下文、工具类和通知扩展 |
| [配置参考](docs/configuration.md) | 配置项、默认值、生效条件、自动配置 Bean 和当前限制 |
| [可选集成](docs/integrations.md) | MyBatis-Plus、分页和 Feign 的完整接入方式 |
| [2.x → 3.x 迁移指南](docs/migration-java17.md) | JDK、框架、Java 调用、JSON API 和已移除能力的迁移步骤 |

## 验证与发布准备

```sh
./mvnw clean verify
./mvnw install
./mvnw -f integration-tests/consumer/pom.xml clean verify
```

最后一条命令使用已安装的真实 JAR 启动独立消费项目，检查 HTTP 调用和可选依赖缺席时的自动配置。普通构建生成 JAR、POM、源码包、Javadoc 包和 `git.properties`。

正式发布使用显式启用的 `central` profile：发布前需要正式版本号、GPG 密钥，以及 Maven settings 中 id 为 `central` 的凭据。该 profile 的 `autoPublish=false`，远程发布权限与凭据尚未验证。Git 提交的 SSH 签名和 Maven 制品的 GPG 签名分别配置。
