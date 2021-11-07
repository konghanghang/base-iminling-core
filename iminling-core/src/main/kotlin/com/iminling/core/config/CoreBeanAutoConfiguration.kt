package com.iminling.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.iminling.common.json.JsonUtil
import com.iminling.core.ApplicationConstant
import com.iminling.core.config.argument.DefaultRequestDataReader
import com.iminling.core.config.argument.GlobalArgumentBeanPostProcessor
import com.iminling.core.config.argument.GlobalArgumentResolverConfig
import com.iminling.core.config.exception.GlobalExceptionHandler
import com.iminling.core.config.jpa.CustomizeJpaConfiguration
import com.iminling.core.config.mybatis.CustomizeMybatisConfig
import com.iminling.core.config.value.GlobalReturnValueHandler
import com.iminling.core.filter.AuthFilter
import com.iminling.core.filter.CustomizeGlobalFilter
import com.iminling.core.filter.Filter
import com.iminling.core.filter.LoginFilter
import com.iminling.core.service.ILogService
import com.iminling.properties.Knife4jApiInfoProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.env.ConfigurableEnvironment
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc


/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
@EnableSwagger2WebMvc
@EnableConfigurationProperties(Knife4jApiInfoProperties::class)
@Import(CustomizeMybatisConfig::class, CustomizeJpaConfiguration::class)
class CoreBeanAutoConfiguration {

    /*@Bean
    @ConditionalOnMissingBean(name = "defaultAuthFilter")
    public AuthFilter defaultAuthFilter() {
        return new DefaultAuthFilter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultLoginFilter")
    public LoginFilter defaultLoginFilter() {
        return new DefaultLoginFilter();
    }*/
    @Bean
    @ConditionalOnMissingBean(CustomizeGlobalFilter::class)
    fun customizeGlobalFilter(): CustomizeGlobalFilter {
        return CustomizeGlobalFilter()
    }

    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler::class)
    fun globalExceptionHandler(): GlobalExceptionHandler {
        return GlobalExceptionHandler()
    }

    @Bean
    @ConditionalOnMissingBean(GlobalReturnValueHandler::class)
    fun globalReturnValueHandler(): GlobalReturnValueHandler {
        return GlobalReturnValueHandler()
    }

    @Bean
    @ConditionalOnMissingBean(DefaultRequestDataReader::class)
    fun defaultRequestDataReader(objectMapper: ObjectMapper): DefaultRequestDataReader {
        return DefaultRequestDataReader(objectMapper)
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return JsonUtil.getInstant()
    }

    @Bean
    @ConditionalOnMissingBean(GlobalArgumentResolverConfig::class)
    fun globalArgumentBeanPostProcessor(): GlobalArgumentBeanPostProcessor? {
        return GlobalArgumentBeanPostProcessor()
    }

    /**
     * 生成globalInterceptor
     * @param environment 环境信息
     * @param defaultRequestDataReader 请求数据读取
     * @param loginFilter 登录验证过滤器，有对应bean则size = 0，不会是null
     * @param authFilter 授权验证过滤器，有对应bean则size = 0，不会是null
     * @param logServices 日志记录服务，有对应bean则size = 0，不会是null
     * @return [GlobalInterceptor]
     */
    @Bean
    @ConditionalOnMissingBean(GlobalInterceptor::class)
    fun globalInterceptor(
        environment: ConfigurableEnvironment,
        defaultRequestDataReader: DefaultRequestDataReader,
        loginFilter: List<LoginFilter>, authFilter: List<AuthFilter>, logServices: MutableList<ILogService>
    ): GlobalInterceptor {
        val filters: MutableList<Filter> = ArrayList(4)
        val enable = "enable"
        if (environment.getProperty(ApplicationConstant.KEY_FILTER_LOGIN, enable) == enable) {
            filters.addAll(loginFilter)
        }
        if (environment.getProperty(ApplicationConstant.KEY_FILTER_AUTH, enable) == enable) {
            filters.addAll(authFilter)
        }
        if (environment.getProperty(ApplicationConstant.KEY_FILTER_LOG, enable) != enable) {
            logServices.clear()
        }
        return GlobalInterceptor(defaultRequestDataReader, filters, logServices, environment)
    }

    /**
     * knife4j配置
     * todo apiInfo配置
     */
    @Bean(value = ["defaultApi2"])
    fun defaultApi2(knife4jApiInfoProperties: Knife4jApiInfoProperties): Docket {
        /*var selectors = mutableListOf<Predicate<RequestHandler>>()
        knife4jApiInfoProperties.packages.forEach { selectors.add(RequestHandlerSelectors.basePackage(it)) }*/
        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(
                ApiInfoBuilder() //.title("swagger-bootstrap-ui-demo RESTful APIs")
                    .description(knife4jApiInfoProperties.description)
                    .termsOfServiceUrl(knife4jApiInfoProperties.serviceUrl)
                    .contact(Contact(knife4jApiInfoProperties.contactName, knife4jApiInfoProperties.contactUrl, knife4jApiInfoProperties.contactEmail))
                    .version(knife4jApiInfoProperties.version)
                    .build()
            ) //分组名称
            .groupName(knife4jApiInfoProperties.groupName)
            .enable(knife4jApiInfoProperties.enable)
            .select() //这里指定Controller扫描包路径
            .apis(RequestHandlerSelectors.basePackage(knife4jApiInfoProperties.basePackage))
            /*.apis {
                var clazz = it.declaringClass()
                if (clazz == null) false else {
                    ClassUtils.getPackageName(clazz).startsWith("")
                }
            }*/
            //.apis(Predicates.or(selectors))
            .paths(PathSelectors.any())
            .build()
    }

}