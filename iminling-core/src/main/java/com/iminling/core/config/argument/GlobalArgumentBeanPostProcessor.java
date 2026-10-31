package com.iminling.core.config.argument;

import com.iminling.common.json.JsonUtil;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.function.Supplier;

public class GlobalArgumentBeanPostProcessor implements BeanPostProcessor {
    private final Supplier<? extends ObjectMapper> mapper;

    public GlobalArgumentBeanPostProcessor() {
        this(JsonUtil::getInstant);
    }

    public GlobalArgumentBeanPostProcessor(Supplier<? extends ObjectMapper> mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) {
        if (bean instanceof RequestMappingHandlerAdapter adapter) {
            var resolvers = new ArrayList<HandlerMethodArgumentResolver>();
            resolvers.add(new GlobalArgumentResolver(mapper.get()));
            if (adapter.getArgumentResolvers() != null)
                resolvers.addAll(adapter.getArgumentResolvers());
            adapter.setArgumentResolvers(resolvers);
        }
        return bean;
    }
}
