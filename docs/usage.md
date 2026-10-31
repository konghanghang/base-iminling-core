# 功能与用法

[返回 README](../README.md) · [配置参考](configuration.md) · [可选集成](integrations.md)

本页说明默认行为和接入方式。独立应用的完整启动示例见 [README](../README.md#快速接入)；MyBatis-Plus、Feign 的依赖和示例见[可选集成](integrations.md)。

## 自定义参数解析

### 策略与优先级

`@EnableResolve` 可放在 Controller 类或方法上。方法上的设置优先，即使值为 `NONE` 也不会回退到类级设置。

| 策略 | 自定义 JSON 参数 | 统一返回 |
| --- | --- | --- |
| `NONE` | 关闭 | 关闭 |
| `ARGUMENTS` | 开启 | 关闭 |
| `RETURN_VALUE`（注解默认值） | 关闭 | 开启 |
| `ALL` | 开启 | 开启 |

方法和类都没有注解时，不启用这两项处理；异常处理、请求日志和其他全局配置仍可生效。

### 请求结构

下面的参数示例不需要 `@RequestBody`：

```java
package example;

import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.constant.ResolveStrategy;
import com.iminling.core.page.PageModel;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchController {
    public record Condition(String name) {}

    @PostMapping("/items/{category}")
    @EnableResolve(ResolveStrategy.ALL)
    public Map<String, Object> search(
            Condition condition,
            PageModel pageModel,
            @PathVariable String category,
            @RequestHeader("X-Tenant") String tenant) {
        return Map.of("name", condition.name(), "page", pageModel.getPageNum(),
                "category", category, "tenant", tenant);
    }
}
```

```sh
curl -X POST http://localhost:8080/items/books \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant: demo' \
  -d '{"condition":{"name":"Java"},"pageModel":{"pageNum":1,"pageSize":10}}'
```

参数处理规则：

- 支持 POST、PUT、PATCH；Content-Type 为 `application/json`、`text/plain`，或缺省。`text/plain` 的内容也必须是合法 JSON。
- 使用方法参数名查找顶层 JSON 属性，保留 `-parameters` 是必要条件。单个 DTO 也需要外层参数名，不能直接省略这一层。
- `@PathVariable`、`@RequestHeader`、ServletRequest / ServletResponse 参数交给 Spring；自定义处理器排在默认参数处理器之前，其他注解不能一概视为会跳过，例如不要将同一参数的 `@RequestParam` 当作 query 参数保留通道。
- 缺少字段或 JSON null 时，引用参数一般得到 null，`Optional<T>` 得到 `Optional.empty()`；只有空实例字段的 Bean 也可能被判断为空。
- 缺少 boolean 基本类型得到 false；缺少数值基本类型得到对应的 0；缺少 char 会报错。若要区分“未提供”和“0”，使用包装类型并结合约束。
- 空请求体不会自动创建 DTO；业务应处理 null，或在参数上添加 `@NotNull` 并启用校验。
- GET、表单和文件上传沿用 Spring 常规方式，不应套用这套 JSON 参数约定。需要常规 `@RequestBody` 的接口可用 `RETURN_VALUE` 仅包装返回值。

## 统一返回与异常

### 返回结构

成功的包装结果为 `ResultModel<T>`，默认成功码为 200，消息为“请求成功”：

```json
{"code":200,"data":{"name":"Alice"},"message":"请求成功"}
```

`ResultModel.ok()`、`fail()`、`fail(String)`、`fail(MessageCode)`、`fail(String, int)` 可直接构造结果，数据通过 `setData()` 设置。无参 `new ResultModel<>()` 的 code 为 0；它不等同于 `ok()`。

下列声明返回类型不自动包装：`ResultModel`、`ResponseEntity`、`StreamingResponseBody`、`SseEmitter`。运行时已经是 `ResultModel` 的响应也避免再次包装。声明返回 String 且开启返回处理时，会输出统一结构的 JSON 文本，并设置 JSON Content-Type。

默认使用 JsonUtil 的格式约定：Long 为字符串，LocalDate / LocalTime / LocalDateTime 分别为 `yyyy-MM-dd` / `HH:mm:ss` / `yyyy-MM-dd HH:mm:ss`。JsonMapper 定制的覆盖边界见[配置参考](configuration.md#定制-jsonmapper)。

### HTTP 状态码与业务 code

HTTP 状态和 JSON 中的 `code` 是两层信息，不能假设永远相等：

| 场景 | 默认 HTTP 状态 | 返回体 |
| --- | --- | --- |
| 普通成功响应包装 | 沿用原响应状态，一般 200 | `code=200`、实际 data |
| Controller 直接返回 `ResultModel.fail(...)` | 未自行设置时一般仍为 200 | 失败 code / message |
| `AuthorizeException` | 取 MessageCode 的 code，例如 401 / 403 | 对应 code / message |
| `BizException` | 500 | 有 MessageCode 时使用它，否则为默认失败 code 500 和异常消息 |
| 参数绑定、缺少请求参数、Bean 校验异常 | 500 | `code=500`，message 为校验信息 |
| 找不到 Handler 或静态资源 | 404 | `code=404`，message 包含路径 |
| 其他 Exception | 500 | 默认系统异常，详细异常写入日志 |

这是当前实现的协议，不是把所有参数错误归为 HTTP 400。消费方应同时检查 HTTP 状态和返回体。

## 登录、权限和请求上下文

### 过滤器扩展

`@LoginRequired`、`@Authentication` 本身不会完成身份验证。实现 `LoginFilter` / `AuthFilter` 并注册为 Spring Bean，才能加入本库的业务拦截器。默认的 `DefaultLoginFilter`、`DefaultAuthFilter` 是空实现，也没有自动注册。

业务过滤器接收 HandlerMethod 和请求，可使用接口的 `getAnnotation()` 读取方法或类上的注解；它支持组合注解，`@Authentication` 上组合了 `@LoginRequired`。

一个需要业务补充 TokenVerifier Bean 的登录示例：

```java
package example;

import com.iminling.core.annotation.LoginRequired;
import com.iminling.core.config.filter.LoginFilter;
import com.iminling.core.constant.MessageCode;
import com.iminling.core.exception.AuthorizeException;
import com.iminling.core.util.ThreadContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class DemoLoginFilter implements LoginFilter {
    public record Account(String id, Set<String> permissions) {}
    public interface TokenVerifier {
        Account verify(String authorization);
    }

    private final TokenVerifier verifier;

    public DemoLoginFilter(TokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public int getOrder() { return 0; }

    @Override
    public void doFilter(HandlerMethod handler, HttpServletRequest request) {
        if (getAnnotation(handler, LoginRequired.class) == null) return;
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            throw new AuthorizeException(MessageCode.TOKEN_NULL);
        }
        Account account = verifier.verify(authorization);
        if (account == null) throw new AuthorizeException(MessageCode.TOKEN_ERROR);
        ThreadContext.setAccount(account);
    }
}
```

权限过滤器同样由业务实现，下面示例只演示 permissions 全部匹配；roles 数组的具体语义由业务定义，并非库内已经实现：

```java
package example;

import com.iminling.core.annotation.Authentication;
import com.iminling.core.config.filter.AuthFilter;
import com.iminling.core.exception.AuthorizeException;
import com.iminling.core.util.ThreadContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class DemoAuthFilter implements AuthFilter {
    @Override
    public int getOrder() { return 0; }

    @Override
    public void doFilter(HandlerMethod handler, HttpServletRequest request) {
        Authentication rule = getAnnotation(handler, Authentication.class);
        if (rule == null) return;
        DemoLoginFilter.Account account = ThreadContext.getAccount(DemoLoginFilter.Account.class);
        if (account == null || !account.permissions().containsAll(Arrays.asList(rule.permissions()))) {
            throw new AuthorizeException();
        }
    }
}
```

Controller 方法可标记 `@Authentication(roles = {}, permissions = {"item:read"})`，或只标记 `@LoginRequired`。实际业务应给上述 Account 集合定义非空约定，并实现 TokenVerifier Bean。

先执行所有登录过滤器，再执行所有权限过滤器；同组的 `getOrder()` 影响 Spring 注入顺序，不跨组重新排序。`filters.login=disable` / `filters.auth=disable` 可分别关闭一组过滤器；配置值必须是字符串 enable / disable，见[配置表](configuration.md#本库读取的配置项)。

业务过滤器对符合条件的 Controller 请求执行，并不由库自动按注解筛选；应像示例那样在业务实现里判断注解。日志排除路径会提前跳过本库的 MVC 拦截器，因此也跳过业务鉴权，见[日志与路径排除](#日志与路径排除)。

### ThreadContext

默认 Servlet 过滤器为当前线程设置 ClientInfo：Authorization 头原值、请求 IP 和请求开始时间；业务可以用 `setAccount()` / `getAccount(Class)` 保存用户，或用 `setAttribute()` / `getAttribute()` 保存属性。

请求过滤器在 finally 中清理上下文，MVC 拦截器在完成时也清理。它使用普通 ThreadLocal，不自动传播到线程池、`@Async` 或其他异步任务；这些任务应显式接收所需数据。脱离 HTTP 请求直接调用这些方法时，应由调用方在 finally 中 `clear()`。

## 参数校验

将 `@Validate` 放在由 Spring 管理的 Controller 方法上，配合 `jakarta.validation` 注解使用。切面会先校验方法参数本身的约束，再对非 null 参数对象执行 Bean 校验。嵌套对象需要 Jakarta `@Valid` 才会级联。

```java
package example;

import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.annotation.Validate;
import com.iminling.core.constant.ResolveStrategy;
import com.iminling.core.validation.group.Create;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
public class ValidationController {
    public record CreateInput(@NotBlank(groups = Create.class) String name) {}

    @PostMapping("/validated-items")
    @EnableResolve(ResolveStrategy.ALL)
    @Validate(Create.class)
    public String create(@NotNull CreateInput input) {
        return input.name();
    }
}
```

- 参数对象约束使用 `@Validate` 指定的分组；未指定时是默认分组。本库提供 Create、Update 两个标记，也可自定义继承 ValidateType 的分组。
- 方法参数本身的约束由 executable validation 按默认分组处理；`@Validate(Create.class)` 不会把这一阶段同步切换为 Create 组。
- 切面匹配的是方法上的注解，虽然注解声明允许 TYPE / FIELD，也不能把类或字段上的单独标记视为会启动同一切面。
- 这是额外的校验入口，不等同于 Spring `@Valid` / `@Validated` 的全部行为；避免在同一接口上重复配置多套方法校验。
- `@DateTimeValid(format = "yyyy-MM-dd")` 用于字符串日期初筛，null 直接通过，非空约束需另加；当前使用长度判断和 SimpleDateFormat 默认宽松解析，不保证严格的日历日期校验。

## 日志与路径排除

### Servlet 请求日志

`CustomizeGlobalFilter` 默认输出 INFO 日志，包含 URI、方法、请求头、查询参数，以及请求后的状态、耗时、缓存的请求/响应正文和异常消息。`ResponseLog` 中两类正文各保留前 500 个字符；请求正文缓存还受过滤器构造的容量限制，并非一定完整。

仅 GET、POST、PUT、DELETE 且未被排除的路径启用这套正文缓存与日志。PATCH 仍能使用自定义 JSON 参数，但当前不在 Servlet 日志方法列表中。

路径排除包括 `/error`、`/do_not_delete/*`、`/fonts/**`、`/css/**`、`/actuator/**`、`/webjars/**`，以及 `.js`、`.gif`、`.jpg`、`.jpeg`、`.ico`、`.css`、`.ttd`、`.png` 后缀。Ant 路径匹配不区分大小写，后缀判断是直接 `endsWith`。

同一 `LogUtils.canLog()` 也用于 GlobalInterceptor：这些路径不会经过本库参数预读及登录/权限过滤器。该路径表不应被当作只影响日志显示的设置。

当前没有生效的 `filters.log` 开关，也没有外部化的路径/请求头列表。请求头过滤表不是自动脱敏规则，Authorization 和业务请求体仍可能被记录；业务接入时应据实际记录内容调整日志方案。

### OkHttp 日志

`OkHttpUtils` 默认加入的拦截器使用 `OKHTTP_0` / `OKHTTP_1` 记录请求、响应、超时耗时和错误。它会读取响应源到缓冲区，再复制缓冲区输出文本；正文没有 Servlet 日志的 500 字符截断，不能将其视作纯流式或大型下载专用的客户端。

可以通过业务自己构造 OkHttpClient、不加入该拦截器来选择其他日志方式；默认拦截器也支持 `setIgnoreHeaders()`，传入小写头名集合。

## 常用工具

### JSON / XML

`JsonUtil.str2Obj()` 支持 Class、Type 和 Jackson 3 TypeReference；`convert()` 支持对象转换；`xml2Obj()`、`Obj2xml()`、`map2xml()` 处理 XML。直接使用 Jackson API 时，core/databind/dataformat 包名为 `tools.jackson.*`，通用注解保留 `com.fasterxml.jackson.annotation.*`。

`obj2Str("text")` 直接返回 `text`，不是包含引号的 JSON 字符串；某些空 Bean 会被它判为空。需要明确的 JSON 编码或异常传播时，直接使用 `JsonUtil.getInstant().writeValueAsString(value)`。`str2Obj` 解析失败通常记录日志并返回 null，不要把 null 自动解释成成功结果。

### HTTP

```java
package example;

import com.iminling.common.http.OkHttpUtils;
import java.io.IOException;
import okhttp3.Request;
import okhttp3.Response;

public final class HttpExample {
    public static String get(String url) throws IOException {
        var client = OkHttpUtils.okHttpClientBuilder().build();
        try (Response response = client.newCall(new Request.Builder().url(url).build()).execute()) {
            return response.body() == null ? "" : response.body().string();
        }
    }
}
```

业务应复用客户端，上例把创建过程放在方法内仅便于展示。证书重载 `okHttpSSLClientBuilder()` 支持文件路径或 InputStream：路径重载自动关闭文件流，InputStream 重载的生命周期由调用方管理。默认超时见[配置参考](configuration.md#http-默认值与固定超时规则)。

core 的默认 RestTemplate 使用 OkHttp 请求工厂、UTF-8 String 转换器、JSON 和 text/plain JSON 转换器。HTTP 错误会被 `RestTemplateErrorHandler` 转成 RuntimeException，消息为解码后并 trim 的响应正文，而非默认的 HttpClientErrorException / HttpServerErrorException。

### 加解密与 ID

AESUtils 提供 ECB / CBC，RSAUtils 提供签名、验签和加解密，MD5Utils / SHAUtils 提供摘要相关操作。参数格式和算法选择按各方法签名使用；Base64 编码、Hashids 和加密是不同用途。

```java
package example;

import com.iminling.common.crypto.AESUtils;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoExample {
    public static String roundTrip(String text) throws GeneralSecurityException {
        byte[] key = Base64.getDecoder().decode(AESUtils.generateECBKey());
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        String encrypted = AESUtils.encryptCBC(text.getBytes(StandardCharsets.UTF_8), key, iv);
        return AESUtils.decryptCBC(Base64.getDecoder().decode(encrypted), key, iv);
    }
}
```

CBC 的 String 参数重载要求 data、key、iv 都是 Base64 编码；byte[] 重载直接接收原始字节。示例为本地往返演示，跨请求使用时需要按业务协议保存/传输 IV 并管理密钥；不要使用仓库中的默认密钥常量作为业务密钥。

`IdUtils.encodeId()` / `decodeId()` 使用 Hashids 做数值 ID 编码；decode 失败或得到 0 时返回 null。Hashids 不是权限控制或密码学加密，不应据此决定用户是否能访问某个对象。

### 缓存和其他工具

`AbstractLocalCache<K,V>` 是 Caffeine 异步加载缓存，业务通过继承并实现 `loadData(K)` 提供数据；`getCache()` 可访问原生缓存接口。默认容量 1,000，访问后过期 300 秒，写后刷新 60 秒。构造器的两个时间参数都以秒为单位，刷新在 Caffeine 的访问/加载规则下触发，并非独立定时全量刷新。

每个实例创建独立单线程执行器，当前没有统一销毁接口，应复用实例。`DefaultLocalCacheImpl` 的加载逻辑只是 key 加时间戳；`CacheUtils` 已弃用，不作为新业务接入示例。

日期、文件、图片/验证码、Map、数值工具的具体签名以源码和 Javadoc 为准，避免复制一份容易失真的完整方法列表。

## 定时任务错误通知

使用 Spring 的 `@EnableScheduling`，且没有业务 TaskScheduler Bean 时，本库会提供带 GlobalSpringErrorHandler 的 ThreadPoolTaskScheduler。已有 TaskScheduler 时，本库退让，不会把错误处理器自动安装到业务调度器上。

通知需要同时满足：`errorHandler.enable=true`、配置接收人，并且存在业务实现的 SendNoticeService Bean。错误首先记录日志，再逐个调用服务与接收人。

```java
package example;

import com.iminling.core.notice.SendNoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoticeService implements SendNoticeService {
    private static final Logger LOG = LoggerFactory.getLogger(NoticeService.class);

    @Override
    public void sendNotice(String chatId, String content) {
        // 示例仅记录日志；业务可在这里连接自己的通知渠道。
        LOG.error("Scheduled task notice, recipient={}, detail={}", chatId, content);
    }
}
```

接收人配置形式见[配置参考](configuration.md#基础配置示例)。`TelegramRobotApi.sendMessage(token, chatId, content)` 是可手动调用的 HTTP 工具，没有自动作为 SendNoticeService 注册；凭据由业务提供，当前方法也没有将 Telegram 响应内容映射成业务成功/失败结果。
