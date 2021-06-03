# base-iminling-core
基础服务包，抽取公用方法，方便自己以后在项目中快速引用。
服务使用springboot版本为2.3.5.RELEASE。整体服务依赖base-iminling-parent父pom，坐标引入如下：
```xml
<parent>
    <groupId>com.iminling</groupId>
    <artifactId>base-iminling-parent</artifactId>
    <version>1.0.3-SNAPSHOT</version>
    <relativePath/>
</parent>
```
引入本基础模块代码如下：
```xml
<dependency>
    <groupId>com.iminling</groupId>
    <artifactId>base-iminling-core</artifactId>
    <version>1.0.2-SNAPSHOT</version>
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

## 使用需知
由于引入了mybatis相关，所以在引入项目的时候如果没有配置数据库相关属性，请在启动类中排除DataSourceAutoConfiguration类，并且需要配置扫描包`com.iminling.core`,详细代码如下：
```java
@SpringBootApplication(scanBasePackages = {"com.iminling.core", "com.test"},
        exclude = DataSourceAutoConfiguration.class)
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}
```

## 功能介绍
### 自定义参数处理
在spring中如果想进行下边的参数写法来传递参数，只能使用form-data形式来传递，如果想使用json形式传递，则需要对这两个对象进行复合，用一个新的类，把这两个类做为其中的属性。
```java
@PostMapping
public IPage list(DiaryCondition condition, PageModel pageModel) {
 }
```
本框架里的参数支持如上这样子进行分开写，然后在方法上或类上标注`@EnableResolve`注解，然后就可以正常运行，也不需要使用@RequestBody注解就可以接受json类型的参数，传参只需要这样子：
```json
{
   "condition": {
      
   },
   "pageModel": {
      
   }
}
```

### 自定义返回值处理
处理返回的结果，给结果统一添加一层固定的格式，详细见`com.iminling.model.common.ResultModel`.

### controller全局异常处理
对异常进行分类处理，最终也返回统一格式，详细见`com.iminling.model.common.ResultModel`.

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

## 更新日志
### 2021-03-11
1. 添加mybatis热加载mapper功能

    使用说明,默认对`mybatis-plus.mapper-locations`属性配置的目录进行加载
   
    需要配置属性`mybatis-plus.refresh-mapper=true`,默认不开启热加载功能
   
    可选属性`mybatis-plus.refresh-mapper-interval=10`,刷新间隔,默认为5s
### 2021-03-25
1. 添加参数和返回值打印(可选，默认关闭)

   配置`application.log.argument`值为true，则开启请求参数打印，默认不打印

   配置`application.log.result`值为true，则开启请求参数打印，默认不打印