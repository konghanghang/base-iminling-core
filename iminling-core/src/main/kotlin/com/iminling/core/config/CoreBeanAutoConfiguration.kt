package com.iminling.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.iminling.common.http.OkHttpUtils
import com.iminling.common.json.JsonUtil
import com.iminling.core.ApplicationConstant
import com.iminling.core.config.argument.DefaultRequestDataReader
import com.iminling.core.config.argument.GlobalArgumentBeanPostProcessor
import com.iminling.core.config.argument.GlobalArgumentResolverConfig
import com.iminling.core.config.exception.GlobalExceptionHandler
import com.iminling.core.config.filter.AuthFilter
import com.iminling.core.config.filter.Filter
import com.iminling.core.config.filter.LoginFilter
import com.iminling.core.config.jpa.CustomizeJpaConfiguration
import com.iminling.core.config.mybatis.CustomizeMybatisConfig
import com.iminling.core.config.rest.EnhanceRestTemplate
import com.iminling.core.config.rest.RestTemplateErrorHandler
import com.iminling.core.config.rest.TextPlainHttpMessageConverter
import com.iminling.core.config.swagger.SwaggerAutoConfiguration
import com.iminling.core.config.value.GlobalReturnValueHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc


/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
@EnableSwagger2WebMvc
@Import(CustomizeMybatisConfig::class, CustomizeJpaConfiguration::class, SwaggerAutoConfiguration::class)
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
     * @return [GlobalInterceptor]
     */
    @Bean
    @ConditionalOnMissingBean(GlobalInterceptor::class)
    fun globalInterceptor(
        environment: ConfigurableEnvironment,
        defaultRequestDataReader: DefaultRequestDataReader,
        loginFilter: List<LoginFilter>, authFilter: List<AuthFilter>
    ): GlobalInterceptor {
        val filters: MutableList<Filter> = ArrayList(4)
        val enable = "enable"
        if (environment.getProperty(ApplicationConstant.KEY_FILTER_LOGIN, enable) == enable) {
            filters.addAll(loginFilter)
        }
        if (environment.getProperty(ApplicationConstant.KEY_FILTER_AUTH, enable) == enable) {
            filters.addAll(authFilter)
        }
        return GlobalInterceptor(defaultRequestDataReader, filters, environment)
    }

    @Bean
    @ConditionalOnMissingBean
    fun mappingJackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
        return MappingJackson2HttpMessageConverter(JsonUtil.getInstant())
    }

    /**
     * 自定义restTemplate客户端
     * 配置https https://blog.csdn.net/zhousheng193/article/details/84830164
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate::class)
    fun restTemplate(environment: ConfigurableEnvironment,
                     mappingJackson2HttpMessageConverter: MappingJackson2HttpMessageConverter): RestTemplate {
        val okHttp3ClientHttpRequestFactory = OkHttp3ClientHttpRequestFactory(OkHttpUtils.okHttpClientBuilder().build())
        var enable = environment.getProperty(ApplicationConstant.KEY_REST_TIMEOUT, "false")
        var restTemplate = EnhanceRestTemplate(enable.toBoolean())
        restTemplate.requestFactory = okHttp3ClientHttpRequestFactory
        //val interceptors = restTemplate.interceptors
        //interceptors.add(RestTemplateLoggingInterceptor())
        restTemplate.errorHandler = RestTemplateErrorHandler()
        val messageConverters = restTemplate.messageConverters
        // messageConverters.removeIf { converter: HttpMessageConverter<*>? -> converter is MappingJackson2HttpMessageConverter }
        var listIterator = messageConverters.listIterator()
        while (listIterator.hasNext()) {
            var next = listIterator.next()
            if (next is MappingJackson2HttpMessageConverter) {
                listIterator.set(mappingJackson2HttpMessageConverter)
            }
            if (next is StringHttpMessageConverter) {
                next.defaultCharset = Charsets.UTF_8
            }
        }
        messageConverters.add(TextPlainHttpMessageConverter(JsonUtil.getInstant()))
        return restTemplate
    }

}