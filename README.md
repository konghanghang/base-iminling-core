# base-iminling-core
基础服务包，抽取公用方法，方便自己以后在项目中快速引用。
服务使用springboot版本为2.3.5.RELEASE。
引入本基础模块代码如下：
```xml
<dependency>
    <groupId>com.iminling</groupId>
    <artifactId>iminling-core</artifactId>
    <version>2.1.0</version>
</dependency>
```
如果引入SNAPSHOT版本则需要在pom中加入以下配置：
```xml
<repositories>
   <repository>
      <id>sonatype-snapshots</id>
      <name>sonatype-snapshots</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
      <snapshots>
          <enabled>true</enabled>
      </snapshots>
   </repository>
</repositories>
```
## 功能
- [x] 自定义参数处理
- [x] 自定义返回值处理 
- [x] controller全局异常处理
- [x] 登录验证过滤器，需自己实现
- [x] 权限验证过滤器，需自己实现
- [x] 日志记录功能，需自己实现
- [x] mybatis返回为map时下划线转驼峰
- [x] 参数校验功能
- [x] mybatis热加载mapper功能
- [x] swagger2接口文档
- [x] spring请求和响应日志打印
- [x] openfeign使用自定义client和解码器
- [ ] 自定义ribbon路由规则

## 使用需知
本jar中使用spring spi机制进行bean的添加，只需引入本依赖就可以了。另外mybatis-plus是可选依赖，如果需要使用mybatis相关则自行添加mybatis-plus-boot-starter依赖。

## 功能介绍
### 自定义参数处理
在spring中如果想进行下边的参数写法来传递参数，只能使用form-data形式来传递，如果想使用json形式传递，则需要对这两个对象进行复合，用一个新的类，把这两个类做为其中的属性。
```java
@PostMapping
public IPage list(DiaryCondition condition, PageModel pageModel) {
 }
```
本框架里的参数支持如上这样子进行分开写，然后在方法上或类上标注`@EnableResolve(value=ResolveStrategy.ARGUMENTS)`注解，然后就可以正常运行，也不需要使用@RequestBody注解就可以接受json类型的参数，传参只需要这样子：
```json
{
   "condition": {
      
   },
   "pageModel": {
      
   }
}
```
@EnableResolve默认只处理返回值，如果想要只处理参数或者同时处理参数和返回值详情见枚举类：`ResolveStrategy`。只处理返回值，则参数按照原spring规范处理。

### 自定义返回值处理
处理返回的结果，给结果统一添加一层固定的格式，详细见`ResultModel`.

### controller全局异常处理
对异常进行分类处理，最终也返回统一格式，详细见`GlobalExceptionHandler`.

### 登录验证过滤器(需自己实现)
需要实现`com.iminling.core.filter.LoginFilter`接口，然后重写`doFilter`和`getOrder`方法，然后查询的用户放入`com.iminling.core.util.ThreadContext`中。

### 权限验证过滤器(需自己实现)
需要实现`com.iminling.core.filter.AuthFilter`接口，实现`doFilter`方法和`getOrder`方法。在controller上使用@Authentication注解(com.iminling.core.annotation.Authentication注解)。

### 日志记录功能，需自己实现
使用@ApiDesc标注在方法上，然后需要自己实现ILogService接口，具体怎么存用户自行扩展，该实现需要交给spring容器去管理。
```java
@ApiDesc(desc = "我是日志详情")
@GetMapping(value = "/aaa")
public void get(){
}
```
### 参数校验功能
因为自定义了参数处理器，所以校验功能单独拿了出来，需要使用本框架的注解@Validate(com.iminling.core.annotation)，使用方法如下：
```java
@PostMapping("/register")
@Validate
public void register(@NotNull RegisterVo model){
}
```

### 添加mybatis热加载mapper功能(2021-03-11)

    使用说明,默认对`mybatis-plus.mapper-locations`属性配置的目录进行加载
   
    需要配置属性`mybatis-plus.refresh-mapper=true`,默认不开启热加载功能
   
    可选属性`mybatis-plus.refresh-mapper-interval=10`,刷新间隔,默认为5s

### swagger2接口文档

引入swagger2文档，兼容自定义参数的形式。添加以下功能：
1. 针对枚举类处理，支持枚举类显示为正常的参数形式，需要在枚举类上添加`@SwaggerDisplayEnum`注解
2. 默认展示的是code和desc，如果不是这两个字段，需要在注解参数中进行指定

### spring请求和响应日志打印

默认对spring的请求和响应都会进行参数打印，包括方法，请求入参，响应结果。