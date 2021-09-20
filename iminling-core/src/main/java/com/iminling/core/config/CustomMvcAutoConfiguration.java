package com.iminling.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
        for (int i = 0; i < converters.size(); i++) {
            HttpMessageConverter<?> httpMessageConverter = converters.get(i);
            if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter) {
                converters.add(0, httpMessageConverter);
                break;
            }
        }
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
