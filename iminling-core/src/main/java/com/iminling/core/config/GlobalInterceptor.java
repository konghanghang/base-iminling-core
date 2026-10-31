package com.iminling.core.config;

import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.config.argument.*;
import com.iminling.core.config.filter.Filter;
import com.iminling.core.constant.ResolveStrategy;
import com.iminling.core.constant.StringEnum;
import com.iminling.core.exception.AuthorizeException;
import com.iminling.core.util.*;

import jakarta.servlet.http.*;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

public class GlobalInterceptor implements HandlerInterceptor {
    private final DefaultRequestDataReader defaultRequestDataReader;
    private final List<Filter> filters;

    public GlobalInterceptor(
            DefaultRequestDataReader reader,
            List<Filter> filters,
            ConfigurableEnvironment environment) {
        this.defaultRequestDataReader = reader;
        this.filters = new ArrayList<>(filters);
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod method) || !LogUtils.canLog(request.getRequestURI()))
            return true;
        EnableResolve resolve = method.getMethodAnnotation(EnableResolve.class);
        if (resolve == null) resolve = method.getBeanType().getAnnotation(EnableResolve.class);
        if (resolve != null
                && (resolve.value() == ResolveStrategy.ALL
                        || resolve.value() == ResolveStrategy.ARGUMENTS)) {
            var message = new ServletServerHttpRequest(request);
            var wrapper = new RequestDataWrapper(false);
            request.setAttribute(StringEnum.REQUEST_DATA_KEY.getDesc(), wrapper);
            if (defaultRequestDataReader.canRead(message)) {
                wrapper.setCanRead(true);
                wrapper.parseJsonNode(defaultRequestDataReader.read(message, method));
            }
        }
        for (Filter filter : filters) {
            try {
                filter.doFilter(method, request);
            } catch (AuthorizeException error) {
                ResponseWriter.write(response, error);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception error) {
        ThreadContext.clear();
    }
}
