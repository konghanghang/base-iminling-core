package com.iminling.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@AutoConfiguration(after = CoreBeanAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CustomizeMvcAutoConfiguration implements WebMvcConfigurer {
    private final GlobalInterceptor globalInterceptor;
    private final tools.jackson.databind.json.JsonMapper mapper;

    public CustomizeMvcAutoConfiguration(
            GlobalInterceptor interceptor, tools.jackson.databind.json.JsonMapper mapper) {
        globalInterceptor = interceptor;
        this.mapper = mapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .ignoreAcceptHeader(true)
                .favorParameter(true)
                .defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void configureMessageConverters(
            org.springframework.http.converter.HttpMessageConverters.ServerBuilder builder) {
        builder.withJsonConverter(
                new JacksonJsonHttpMessageConverter(mapper) {
                    @Override
                    protected void writeInternal(
                            Object value,
                            org.springframework.core.ResolvableType type,
                            HttpOutputMessage message,
                            java.util.Map<String, Object> hints)
                            throws IOException {
                        if (value instanceof String text)
                            StreamUtils.copy(text, StandardCharsets.UTF_8, message.getBody());
                        else super.writeInternal(value, type, message, hints);
                    }
                });
    }

    @Bean
    public CorsFilter corsFilter() {
        var source = new UrlBasedCorsConfigurationSource();
        var config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
