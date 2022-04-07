package com.iminling.core.config

import com.iminling.common.json.JsonUtil
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.MimeTypeUtils
import org.springframework.util.StreamUtils
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.IOException
import java.lang.reflect.Type
import java.nio.charset.Charset

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
// @EnableWebMvc
class CustomMvcAutoConfiguration(var globalInterceptor: GlobalInterceptor): WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(globalInterceptor)
                // 可以通过excludePathPatterns过滤掉某些路径
            .excludePathPatterns("/swagger-ui.html")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/")
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
    }

    override fun addArgumentResolvers(resolvers: List<HandlerMethodArgumentResolver>) {
        // resolvers.add(new RequestArgumentResolver(objectMapper));
    }

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer.ignoreAcceptHeader(true)
            .useRegisteredExtensionsOnly(true)
            .favorParameter(true)
            .defaultContentType(MediaType.ALL)
            .mediaType(MimeTypeUtils.ALL_VALUE, MediaType.ALL)
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(0, object : MappingJackson2HttpMessageConverter(JsonUtil.getInstant()) {
            /**
             * 重写Jackson消息转换器的writeInternal方法
             * SpringMVC选定了具体的消息转换类型后,会调用具体类型的write方法,将Java对象转换后写入返回内容
             */
            @Throws(IOException::class, HttpMessageNotWritableException::class)
            override fun writeInternal(rs: Any, type: Type?, outputMessage: HttpOutputMessage) {
                if (rs is String) {
                    // 参考StringHttpMessageConverter
                    // Charset charset = this.getContentTypeCharset(outputMessage.getHeaders().getContentType());
                    StreamUtils.copy(rs, Charsets.UTF_8, outputMessage.body)
                } else {
                    super.writeInternal(rs, type, outputMessage)
                }
            }
        })
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        // 对接口配置跨域设置
        source.registerCorsConfiguration("/**", buildConfig())
        return CorsFilter(source)
    }

    private fun buildConfig(): CorsConfiguration {
        val corsConfiguration = CorsConfiguration()
        // 允许任何域名使用
        corsConfiguration.addAllowedOrigin("*")
        // 允许任何头
        corsConfiguration.addAllowedHeader("*")
        // 允许任何方法（post、get等）
        corsConfiguration.addAllowedMethod("*")
        return corsConfiguration
    }

}