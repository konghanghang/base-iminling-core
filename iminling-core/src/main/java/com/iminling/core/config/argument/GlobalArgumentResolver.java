package com.iminling.core.config.argument;

import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.constant.ResolveStrategy;
import com.iminling.core.constant.StringEnum;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.*;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

public class GlobalArgumentResolver implements HandlerMethodArgumentResolver {
    private final ObjectMapper objectMapper;

    public GlobalArgumentResolver(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attrs)) return false;
        Class<?> type = parameter.getParameterType();
        if (ServletRequest.class.isAssignableFrom(type)
                || ServletResponse.class.isAssignableFrom(type)
                || parameter.hasParameterAnnotation(PathVariable.class)
                || parameter.hasParameterAnnotation(RequestHeader.class)) return false;
        Object value = attrs.getRequest().getAttribute(StringEnum.REQUEST_DATA_KEY.getDesc());
        if (!(value instanceof RequestDataWrapper wrapper) || !wrapper.getCanRead()) return false;
        EnableResolve annotation = parameter.getMethodAnnotation(EnableResolve.class);
        if (annotation == null)
            annotation = parameter.getContainingClass().getAnnotation(EnableResolve.class);
        return annotation != null
                && (annotation.value() == ResolveStrategy.ALL
                        || annotation.value() == ResolveStrategy.ARGUMENTS);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer container,
            NativeWebRequest request,
            WebDataBinderFactory binder) {
        RequestDataWrapper wrapper =
                (RequestDataWrapper)
                        request.getAttribute(
                                StringEnum.REQUEST_DATA_KEY.getDesc(),
                                RequestAttributes.SCOPE_REQUEST);
        if (wrapper == null)
            throw new IllegalArgumentException(
                    "无法注入参数，当前不是JSON请求: " + parameter.getParameterName());
        MethodParameter nested = parameter.nestedIfOptional();
        Object value = null;
        if (wrapper.getHasParams()
                && wrapper.getParams().hasNonNull(parameter.getParameterName())) {
            value =
                    objectMapper.convertValue(
                            wrapper.getParams().path(parameter.getParameterName()),
                            objectMapper.constructType(nested.getNestedGenericParameterType()));
            if (com.iminling.common.json.JsonUtil.objIsNull(value)) value = null;
        }
        if (parameter.getParameterType() == Optional.class) return Optional.ofNullable(value);
        if (value == null && parameter.getParameterType().isPrimitive()) {
            Class<?> type = parameter.getParameterType();
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0F;
            if (type == double.class) return 0D;
            throw new IllegalStateException(
                    "Missing primitive parameter: " + parameter.getParameterName());
        }
        return value;
    }
}
