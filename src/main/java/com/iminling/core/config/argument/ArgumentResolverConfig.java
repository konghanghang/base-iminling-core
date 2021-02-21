package com.iminling.core.config.argument;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义的参数处理器放在第一位
 */
@Configuration
public class ArgumentResolverConfig {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    private final ObjectMapper objectMapper;

    public ArgumentResolverConfig(RequestMappingHandlerAdapter requestMappingHandlerAdapter, ObjectMapper objectMapper) {
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void argumentResolver() {
        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
        argumentResolvers.add(new RequestArgumentResolver(objectMapper));
        argumentResolvers.addAll(requestMappingHandlerAdapter.getArgumentResolvers());
        requestMappingHandlerAdapter.setArgumentResolvers(argumentResolvers);
    }
}
