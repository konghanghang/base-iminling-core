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

## 更新日志
### 2021-03-11
1. 添加mybatis热加载mapper功能

    使用说明,默认对`mybatis-plus.mapper-locations`属性配置的目录进行加载
   
    需要配置属性`mybatis-plus.refresh-mapper=true`,默认不开启热加载功能
   
    可选属性`mybatis-plus.refresh-mapper-interval=10`,刷新间隔,默认为5s