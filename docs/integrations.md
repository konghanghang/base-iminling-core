# 可选集成

[返回 README](../README.md) · [功能与用法](usage.md) · [配置参考](configuration.md)

`iminling-core` 将 MyBatis-Plus、Feign 和 LoadBalancer 标记为 optional。消费项目必须显式引入所需组件；不使用它们时，仅引入 core 即可启动基础 MVC 功能。

本页的版本与当前基线对应：Spring Boot 4.0.9、Spring Cloud 2025.1.4、MyBatis-Plus 3.5.16。示例依赖片段需合并到消费项目的 POM，并保留 Java 17 和参数名编译配置。

## MyBatis-Plus

### 依赖和数据源

```xml
<dependencies>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
        <version>3.5.16</version>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-jsqlparser</artifactId>
        <version>3.5.16</version>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

本库配置以 MyBatis-Plus 自动配置和分页插件类同时存在为条件，因此不要遗漏单独的 `mybatis-plus-jsqlparser`。数据库驱动由消费项目选择，不能依赖本库测试范围的 H2。

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/example
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

mybatis:
  sql:
    log: false

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  refresh-mapper: false
```

Mapper 使用 MyBatis 标准 `@Mapper` 或消费项目自己的 `@MapperScan`。表结构、数据库权限与连接池参数由消费项目维护。

### 实体与时间字段填充

下面是 `ItemEntity.java`，对应业务表 `doc_item`；该表应包含 id、name、create_time、update_time 列。

```java
package example;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("doc_item")
public class ItemEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime value) { createTime = value; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime value) { updateTime = value; }
}
```

本库的 MetaObjectHandler 使用严格填充：INSERT 时尝试填充空的 `createTime`，UPDATE 时尝试填充空的 `updateTime`；字段类型为 LocalDateTime，并需配置相应 FieldFill 元数据。已有非 null 值不会强行覆盖，不是每次 UPDATE 都无条件重写时间。不会自动分配业务 ID。

业务提供自己的 MetaObjectHandler Bean 后，本库默认处理器退让。

### 分页与 Mapper

`ItemMapper.java`：

```java
package example;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper extends BaseMapper<ItemEntity> {}
```

`ItemController.java`：

```java
package example;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.constant.ResolveStrategy;
import com.iminling.core.page.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ItemController {
    private final ItemMapper mapper;

    public ItemController(ItemMapper mapper) { this.mapper = mapper; }

    @PostMapping("/db/items")
    @EnableResolve(ResolveStrategy.ALL)
    public PageResp<ItemEntity> list(PageModel pageModel) {
        Page<ItemEntity> page = mapper.selectPage(
                new Page<>(pageModel.getPageNum(), pageModel.getPageSize()),
                new QueryWrapper<>());
        return MPageBuilder.builder(page);
    }
}
```

请求示例为 `{"pageModel":{"pageNum":1,"pageSize":10}}`。PageModel 默认页码 1、每页 10 条；PageResp 包含 pageModel 和 list，MPageBuilder 从 IPage 复制分页元数据。getOffset() 计算 `(pageNum - 1) * pageSize`，不参与 JSON 输出。

本库默认分页插件固定为 MySQL 方言。其他数据库应提供自己的 MybatisPlusInterceptor Bean，并加入合适的分页插件；一旦提供业务拦截器，本库不会再自动补装分页插件。

### Map 键转换、SQL 日志与 XML 热加载

- Map 结果包装器在 `useCamelCaseMapping` 为 true 时，将下划线键名转换为驼峰。该条件取决于 MyBatis 的映射设置，不能仅凭注册了包装器就认为所有 Map 键都会转换。
- 本库以 ConfigurationCustomizer 设置 ObjectWrapperFactory。如果业务也定制相同属性，需要协调多个 customizer 的最终结果；这个 Bean 不按同类型自动退让。
- SQL 日志默认开启，记录语句、参数和耗时。用 `mybatis.sql.log=false` 关闭本库的 SqlInterceptor；它与 MyBatis 原生日志配置是不同入口。
- XML mapper 热加载默认关闭，按 MyBatis-Plus 的 `mapper-locations` 收集资源。启用后按秒轮询资源修改时间，Bean 销毁时停止后台任务。

开发期可显式开启：

```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  refresh-mapper: true
  refresh-mapper-interval: 5
```

热加载依赖 MyBatis / MyBatis-Plus 的内部映射结构和资源时间戳，用于开发期修改 XML，不是生产环境无停机迁移机制。当前 H2 回归验证覆盖基础 Map 查询与 XML 重载，未代表所有动态 SQL、复杂缓存和生产数据库组合都已验证。

## Feign 与负载均衡

### 依赖与服务发现

消费项目导入对应的 Cloud BOM，并显式添加三个依赖：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2025.1.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-okhttp</artifactId>
    </dependency>
</dependencies>
```

本库没有注册中心或 Nacos 专用自动配置。使用服务名时，由消费项目选择与目标框架兼容的服务发现组件，并提供实际实例信息；仅添加 LoadBalancer 不会自动找到服务。

### 声明 Client

使用固定 URL 的最小 Client 示例 `ItemClient.java`：

```java
package example;

import com.iminling.core.config.value.ResultModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "item-service", url = "${services.items.url}")
public interface ItemClient {
    record Item(String name) {}

    @GetMapping("/items/current")
    Item current();

    @GetMapping("/items/current")
    ResultModel<Item> currentEnvelope();
}
```

在消费项目通过 `FeignConfiguration.java` 启用扫描：

```java
package example;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = ItemClient.class)
public class FeignConfiguration {}
```

```yaml
services:
  items:
    url: http://localhost:8081
```

请求映射放在方法上。需要按服务名发现实例时移除固定 url，并在业务项目配置服务发现。自定义 HTTP Client 基于官方 Feign OkHttp adapter；存在 LoadBalancerClient / LoadBalancerClientFactory Bean 时使用 FeignBlockingLoadBalancerClient 包装，否则使用直接 HTTP Client。

### 返回体解包

启用本库 Decoder 时：

| 方法返回类型 / 响应 | 行为 |
| --- | --- |
| `ResultModel<Item>` | 保留完整结构，由标准 Decoder 按泛型解码 |
| `Item`，响应为 `{"code":200,"data":{"name":"A"}}` | 提取 data 再交给标准 Decoder |
| `String`，data 为 JSON 字符串 | 直接返回 data 的字符串值 |
| 空 body、JSON null 或 data 为 null | 返回 null |
| 普通 JSON，如 `{"name":"A"}` | 不解包，交回标准 Decoder |

解包用 code / data 两个字段的存在性识别结构，不校验业务 code 是否成功。需要将业务失败码变成异常时，由消费项目明确处理；Feign 的非 2xx 响应仍走其错误处理流程。该 Decoder 会先读取和解析 JSON，不是任意纯文本或大文件流式传输的解码器。

本库还提供 WebMvcRegistrations，避免 FeignClient 接口被识别成 MVC Controller。如果业务有自己的 WebMvcRegistrations，需要协调实现，不能假设这一项会按同类型 Bean 自动退让。

### 开关与定制

- `app.customize.feign=false` 关闭整组本库 Feign 自动配置，不只关闭 data 解包；Spring Cloud 本身是否创建默认 Client / Decoder 由它自己的配置决定。
- 已有业务 Client / Decoder Bean 时，本库对应默认 Bean 退让。业务自己提供 Client 时，需自行确认负载均衡和超时语义。
- 默认 Feign Client 的 OkHttp 连接失败重试关闭；连接、读取超时由官方 adapter 根据 Feign Request.Options 处理。业务可用目标 OpenFeign 版本支持的 `spring.cloud.openfeign.client.config` 设置各 Client 参数。

真实服务发现、注册中心与远端调用需要在消费项目验收；仓库已有测试覆盖响应解包和本地 HTTP 请求，不替代外部环境验证。
