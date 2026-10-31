# Java 17 / Maven 迁移记录

基线：`master` / `9f2cb7d`。用户确认最低支持 Java 17，源码迁为纯 Java，构建迁回 Maven，Spring Boot 升级大版本。

## 目标

- 新库版本 `3.0.0-SNAPSHOT`，编译 `release=17`，保留 common / core 两模块。
- Spring Boot 4.0.9 / Spring Cloud 2025.1.4（Cloud BOM 对应的 Boot 版本）。
- Kotlin 源码与测试转 Java；移除 Gradle 和 Kotlin 编译配置。
- Jakarta、Jackson 3、Spring Cloud LoadBalancer 与新版 MyBatis-Plus 适配；按用户后续要求移除 JPA/QueryDSL 和 Swagger/OpenAPI。
- 验证统一返回、自定义参数、过滤器、自动配置和可选集成，文档说明旧版调用方的迁移点。

## 状态

已完成本地实施与验证，提交状态以 Git 记录为准；尚未推送或发布。用户补充要求：只用本机 JDK 17 验证，不再下载或安装其他 JDK。后续确认去掉 JPA 和 Swagger，其自动配置、依赖、辅助类、扩展及专用测试均已移除。

## 实际实现

- 根 POM 和两个子模块统一到 `3.0.0-SNAPSHOT`，Maven Wrapper 固定 3.9.11，Enforcer 要求 Java 17+ / Maven 3.9+，编译目标 17。
- 保留能力的 Kotlin 主源码与测试已迁为 Java；移除 Gradle 文件和旧 `spring.factories` 自动配置注册。
- 使用 Jakarta 与 Jackson 3；保留 JSON 日期格式和 Long 字符串约定，适配 Java 17 的反射访问限制。
- 使用 Spring Cloud LoadBalancer 和受支持的 Feign OkHttp adapter；MyBatis-Plus / Feign 保持可选。
- Spring 7 的 RestTemplate 接口与请求工厂已适配；XML mapper 热加载经过真实查询验证，后台任务随 Bean 关闭。
- 普通构建不要求签名；Central 签名与发布配置放入显式启用的 `central` profile。远程发布尚未执行、未验证。
- JPA/QueryDSL（含 JPageBuilder）、Swagger/OpenAPI（含注解、Knife4j 元信息、UI、定制 schema）已移除，清理相应版本属性和 MVC 路径例外。
- README 说明新基线、调用方迁移点和可选依赖。OkHttp 自身仍会传递引入 Kotlin 标准库；项目源码和编译配置不使用 Kotlin。

## 验证与审查

针对 `9f2cb7d` 之上的本次迁移全部变更（含新增文件），使用本机 Homebrew OpenJDK 17.0.13 和 Maven Wrapper 3.9.11。依赖缓存及验证用 settings 位于 `/private/tmp/iminling-migration`，没有更改系统 Maven / JDK 配置。

- `clean verify` 成功：common 26 个测试通过；core 9 个通过、1 个外部 Telegram 发送测试禁用。共 35 个通过、1 个跳过。
- 覆盖 JSON/XML、AES、MVC 嵌套参数及路径/请求头、统一返回、校验、鉴权失败、请求上下文清理、Feign 解包、RestTemplate 本地 HTTP、H2 + MyBatis mapper 重载、可选依赖缺席。
- 安装到临时本地仓库后，`integration-tests/consumer` 的独立消费测试通过：真实 JAR、真实 HTTP 和自动配置，确认类路径没有 JPA、QueryDSL、springdoc、Swagger，也没有可选的 MyBatis / Feign。
- 两模块全部 class 主版本为 61；JAR 内含 Maven POM、`git.properties`，core 含新版自动配置清单；源码包和 Javadoc 包均已生成；无 Kotlin 源码或 module 元数据；JAR 与自动配置清单无 JPA/Swagger 遗留。
- `git diff --check` 通过；核对旧 Kotlin 类的 Java 替代、依赖传播、自动配置注册和已识别关键路径。

未覆盖真实 Nacos、远程服务发现、生产数据库及 Maven Central 发布权限；不将本地测试视为这些外部环境已经验收。

## 验收入口

`./mvnw clean verify`；检查 JAR / POM / 源码包 / 文档包和 class 主版本 61；下游最小 Spring Boot 应用验证自动配置、MVC，核对已移除组件缺席和无可选依赖时启动行为。
