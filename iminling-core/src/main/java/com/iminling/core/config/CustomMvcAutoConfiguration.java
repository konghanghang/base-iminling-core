package com.iminling.core.config;

import com.iminling.common.json.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author yslao@outlook.com
 * @since 2021/6/12
 */
// @EnableWebMvc
public class CustomMvcAutoConfiguration implements WebMvcConfigurer {

    private final GlobalInterceptor globalInterceptor;

    public CustomMvcAutoConfiguration(GlobalInterceptor globalInterceptor) {
        this.globalInterceptor = globalInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // resolvers.add(new RequestArgumentResolver(objectMapper));
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.ignoreAcceptHeader(true)
                .useRegisteredExtensionsOnly(true)
                .favorParameter(true)
                .defaultContentType(MediaType.ALL)
                .mediaType(MimeTypeUtils.ALL_VALUE, MediaType.ALL);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new MappingJackson2HttpMessageConverter(JsonUtil.getInstant()) {
            /**
             * 重写Jackson消息转换器的writeInternal方法
             * SpringMVC选定了具体的消息转换类型后,会调用具体类型的write方法,将Java对象转换后写入返回内容
             */
            @Override
            protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                if (object instanceof String){
                    // 参考StringHttpMessageConverter
                    // Charset charset = this.getContentTypeCharset(outputMessage.getHeaders().getContentType());
                    StreamUtils.copy((String)object, Charset.defaultCharset(), outputMessage.getBody());
                }else{
                    super.writeInternal(object, type, outputMessage);
                }
            }
            /*private Charset getContentTypeCharset(MediaType contentType) {
                return contentType != null && contentType.getCharset() != null?contentType.getCharset():this.getDefaultCharset();
            }*/
        });
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对接口配置跨域设置
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许任何域名使用
        corsConfiguration.addAllowedOrigin("*");
        // 允许任何头
        corsConfiguration.addAllowedHeader("*");
        // 允许任何方法（post、get等）
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

}
