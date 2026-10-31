package com.iminling.core.config.argument;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;

/** Alternative explicit registration for applications supplying their own MVC configuration. */
public class GlobalArgumentResolverConfig {
    public GlobalArgumentResolverConfig(RequestMappingHandlerAdapter adapter, ObjectMapper mapper) {
        var resolvers = new ArrayList<HandlerMethodArgumentResolver>();
        resolvers.add(new GlobalArgumentResolver(mapper));
        if (adapter.getArgumentResolvers() != null)
            resolvers.addAll(adapter.getArgumentResolvers());
        adapter.setArgumentResolvers(resolvers);
    }
}
