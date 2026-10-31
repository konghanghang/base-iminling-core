# 配置参考

[返回 README](../README.md) · [功能与用法](usage.md) · [可选集成](integrations.md)

本页对应当前 `3.x` 实现，只列出代码实际读取或判断的配置项。业务项目自行配置数据源、服务发现和框架原生参数。

## 本库读取的配置项

| 配置项 | 类型 / 默认值 | 作用与生效条件 |
| --- | --- | --- |
| `filters.login` | 字符串，`enable` | 只有值恰好为 `enable` 才加入业务 `LoginFilter` Bean；设置 `disable` 关闭。不是布尔配置 |
| `filters.auth` | 字符串，`enable` | 同上，控制业务 `AuthFilter` Bean |
| `app.customize.feign` | 布尔，`true` | 在 Feign 和 LoadBalancer 类存在时启用整组 Feign 自动配置；`false` 同时关闭本库的 Client、Decoder 和 MVC 接口排除扩展 |
| `mybatis.sql.log` | 布尔，`true` | MyBatis-Plus 自动配置生效时注册 SQL 日志拦截器 |
| `mybatis-plus.refresh-mapper` | 布尔，`false` | 启用开发期 XML mapper 热加载；要求 MyBatis-Plus 及 `SqlSessionFactory` 可用 |
| `mybatis-plus.refresh-mapper-interval` | 整数，`5` | 热加载扫描间隔，单位秒；必须大于 0，仅启用热加载时读取 |
| `application.rest.timeout.enable` | 布尔，`false` | 启用默认 `EnhanceRestTemplate` 内的固定 URL 超时规则，当前只有 GET `/index`；不是通用 URL 配置绑定 |
| `errorHandler.enable` | 布尔，`false` | 允许本库的定时任务错误处理器调用通知服务；关闭时仍记录错误日志 |
| `error.notice.user` | 逗号分隔字符串，未配置 | 通知接收人。当前直接用 Environment 转成 List，不按配置属性对象绑定；推荐使用字符串形式 |

### 基础配置示例

```yaml
filters:
  login: enable
  auth: enable

mybatis:
  sql:
    log: false

mybatis-plus:
  refresh-mapper: false
  refresh-mapper-interval: 5

app:
  customize:
    feign: true
```

MyBatis、Feign 相关键只有在对应可选依赖和自动配置生效时才起作用，添加键本身不会引入组件。

通知设置推荐使用 `.properties` 的直接属性形式：

```properties
errorHandler.enable=true
error.notice.user=ops-user-1,ops-user-2
```

`error.notice.user` 是业务 `SendNoticeService` 接收的目标标识，不固定代表 Telegram Chat ID。当前通知代码直接读取该属性，不能把 YAML 的 `user: [a, b]` 或索引数组绑定视为已经支持。

## 自动配置与 Bean 覆盖

注册入口为 [AutoConfiguration.imports](../iminling-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports)。

| 自动配置 | 条件 | 注册内容与覆盖边界 |
| --- | --- | --- |
| `CoreBeanAutoConfiguration` | Servlet Web 应用 | 校验切面、请求日志过滤器、异常与返回处理、JsonMapper、请求读取器、参数处理器、业务拦截器和 RestTemplate；主要 Bean 使用 `@ConditionalOnMissingBean` |
| `CustomizeMvcAutoConfiguration` | Servlet Web 应用 | 注册业务拦截器、消息转换、静态资源、内容协商和 `corsFilter`；这一组没有单独的布尔总开关 |
| `CustomizeMybatisConfig` | MyBatis-Plus 自动配置与分页插件类存在 | 分页拦截器、填充处理器、Map 包装配置，以及受配置项控制的 SQL 日志和 mapper 热加载 |
| `CustomizeFeignAutoConfiguration` | Feign / LoadBalancer 类存在，且未关闭 `app.customize.feign` | 默认 Client、Decoder 和 Feign 接口的 MVC 排除；Client / Decoder 支持业务 Bean 覆盖 |

core 对 JsonMapper、RestTemplate 等基础 Bean 的默认实现，可以通过业务提供同类型 Bean 覆盖。`GlobalArgumentBeanPostProcessor` 还会在已有 `GlobalArgumentResolverConfig` 时退让，避免这两种注册方式同时生效。

MyBatis 的 `MybatisPlusInterceptor` 和 `MetaObjectHandler` 支持同类型 Bean 覆盖。业务自己提供 `MybatisPlusInterceptor` 后，需要自行加入分页插件；本库不会再向业务拦截器补装它。Map 的 `ConfigurationCustomizer`、SQL 日志和热加载不能都按“同类型 Bean 自动退让”理解，详见[可选集成](integrations.md)。

### 定制 JsonMapper

```java
package example;

import com.iminling.common.json.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class JsonConfiguration {
    @Bean
    public JsonMapper applicationJsonMapper() {
        return JsonMapper.builder()
                .configureForJackson2()
                .addModule(JsonUtil.conventions())
                .build();
    }
}
```

`JsonUtil.conventions()` 包含 Long 转字符串和日期格式规则。Jackson 3 通过 builder 构建 mapper，不能沿用 Jackson 2 的原地修改方式。

业务 JsonMapper Bean 会用于本库参数转换、MVC 常规对象消息转换和默认 RestTemplate。`JsonUtil.getInstant()` 仍是静态共享实例，直接调用 JsonUtil，以及部分字符串返回、授权响应和 Feign 解包路径，不会自动切换到业务 Bean。不要据此假定所有 JSON 路径都能由一个业务 Bean 完全接管。

## 默认 Web 行为

| 行为 | 当前实现 |
| --- | --- |
| 参数名 | 依赖消费项目的 `-parameters`；本库 POM 设置为 true，不会自动修改独立消费项目的编译参数 |
| JSON | Long 输出字符串；日期 `yyyy-MM-dd`、时间 `HH:mm:ss`、日期时间 `yyyy-MM-dd HH:mm:ss` |
| 内容协商 | 忽略 `Accept` 请求头，启用参数式协商，默认 JSON |
| CORS | 注册名为 `corsFilter` 的 Bean，允许任意 Origin、请求头和方法；未启用允许携带凭据 |
| 静态资源 | 添加 `/static/**` → `classpath:/static/` |
| 业务过滤器顺序 | 先登录过滤器，再权限过滤器；同组 Bean 由 Spring 排序，跨组不按 `getOrder()` 混排 |
| 日志排除路径 | 同时影响 MVC 业务拦截器；排除路径不会经过本库参数预读及业务登录/权限过滤器 |

`corsFilter` 没有 `@ConditionalOnMissingBean`，因此不能简单再声明同名 Bean 并期望本库退让。若应用完全接管 MVC，可通过 Spring Boot 标准配置排除 `com.iminling.core.config.CustomizeMvcAutoConfiguration`，再自行注册所需的拦截器、消息转换与 CORS；排除这一配置会同时移除它承担的其他 MVC 设置。默认 JsonMapper、异常处理等还由 Core 配置提供。

## HTTP 默认值与固定超时规则

`OkHttpUtils.okHttpClientBuilder()` 的默认值：

| 参数 | 默认值 |
| --- | --- |
| 连接超时 | 5,000 毫秒 |
| 读取超时 | 10,000 毫秒 |
| 写入超时 | 10,000 毫秒 |
| 连接失败重试 | 开启；本库 Feign Client 显式关闭 |
| 连接池 | 最多 5 个空闲连接，保持 300 秒 |
| 日志 | 默认加入 `OkHttpLoggingInterceptor` |

开启 `application.rest.timeout.enable` 时，默认 RestTemplate 对 GET、路径匹配 `/index` 的请求使用连接 1,000 / 读取 15,000 / 写入 1,000 毫秒。匹配只看方法和路径，不区分目标主机。其他请求保持默认工厂行为。

这些 URL 规则目前写在 `EnhanceRestTemplate.initUrl()` 中，`UrlProperties` 没有配置文件绑定。需要业务超时时，可提供自己的 RestTemplate Bean 或直接配置 OkHttp Builder。

## 未实现的配置能力

- `filters.log` 目前只有常量声明，没有实际读取与控制逻辑，配置它不会关闭日志。
- 日志路径、请求方法和请求头排除表目前写在代码中，没有通用 YAML 列表绑定。可通过日志框架控制对应 Logger 级别；这只影响输出，不关闭过滤器的缓存与处理逻辑。
- 本库没有内置“按注解自动完成登录、角色或权限判断”的实现，需要业务注册过滤器。
- 本库没有 JPA、QueryDSL、Swagger、OpenAPI 或 Nacos 专用自动配置。
