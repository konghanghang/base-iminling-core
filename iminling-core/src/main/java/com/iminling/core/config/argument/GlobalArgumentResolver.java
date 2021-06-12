package com.iminling.core.config.argument;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.iminling.common.json.JsonUtil;
import com.iminling.core.annotation.EnableResolve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.*;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * 自定义参数处理器
 */
public class GlobalArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String REQUEST_DATA_KEY = GlobalArgumentResolver.class.getName() + ".requestData";

    private final Logger logger = LoggerFactory.getLogger(GlobalArgumentResolver.class);

    private ObjectMapper objectMapper;

    public GlobalArgumentResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (!supportsParameter(request, parameter)) {
            return false;
        }
        EnableResolve enableResolve = parameter.getMethodAnnotation(EnableResolve.class);
        if (Objects.isNull(enableResolve)) {
            enableResolve = parameter.getContainingClass().getAnnotation(EnableResolve.class);
        }
        if (Objects.isNull(enableResolve)) {
            return false;
        }
        EnableResolve.ResolveStrategy value = enableResolve.value();
        return value == EnableResolve.ResolveStrategy.ALL || value == EnableResolve.ResolveStrategy.ARGUMENTS;
    }

    protected boolean supportsParameter(HttpServletRequest request, MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        if (HttpServletResponse.class.isAssignableFrom(parameterType)
                || HttpServletRequest.class.isAssignableFrom(parameterType)) {
            return false;
        }
        // 特殊处理@PathVariable注解
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (Objects.nonNull(pathVariable)) {
            return false;
        }
        // 特殊处理@RequestHeader注解
        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (Objects.nonNull(requestHeader)) {
            return false;
        }
        RequestDataWrapper requestDataWrapper = (RequestDataWrapper) request.getAttribute(REQUEST_DATA_KEY);
        return requestDataWrapper != null && requestDataWrapper.isCanRead();
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Assert.state(mavContainer != null, "ModelAttributeMethodProcessor requires ModelAndViewContainer");
        Assert.state(binderFactory != null, "ModelAttributeMethodProcessor requires WebDataBinderFactory");

        RequestDataWrapper requestDataWrapper = this.getRequestData(webRequest);
        if (requestDataWrapper == null) {
            String path = null;
            if (webRequest instanceof ServletWebRequest) {
                path = ((ServletWebRequest)webRequest).getRequest().getServletPath();
            }
            throw new IllegalArgumentException(String.format("无法注入'%s'参数， 当前不是JSON请求， path = %s",
                    parameter.getParameterName(), path != null ? path : parameter.getMethod()));
        }
        parameter = parameter.nestedIfOptional();
        Object arg = readWithRequestData(requestDataWrapper, parameter, parameter.getNestedGenericParameterType());
        arg = handleNullValue(parameter.getParameterName(), arg, parameter.getParameterType());
        return adaptArgumentIfNecessary(arg, parameter);
    }

    private RequestDataWrapper getRequestData(NativeWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }
        return (RequestDataWrapper) webRequest.getAttribute(REQUEST_DATA_KEY, RequestAttributes.SCOPE_REQUEST);
    }

    protected Object readWithRequestData(RequestDataWrapper requestDataWrapper, MethodParameter parameter, Type parameterType) throws Exception {
        Object result = null;
        if (requestDataWrapper.isHashParams()) {
            JsonNode params = requestDataWrapper.getParams();
            String parameterName = parameter.getParameterName();
            if (params.hasNonNull(parameterName)) {
                JavaType javaType = getJavaType(parameterType, null);
                JsonNode value = params.path(parameterName);
                // 这里可能还需要捕捉异常
                Object obj = objectMapper.convertValue(value, javaType);
                // 判断对象里边的key是不是都为null
                if (JsonUtil.objIsNull(obj)){
                    return null;
                }
                result = obj;
            }
        }
        return result;
    }

    protected JavaType getJavaType(Type type, @Nullable Class<?> contextClass) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        return typeFactory.constructType(GenericTypeResolver.resolveType(type, contextClass));
    }

    private Object handleNullValue(String name, @Nullable Object value, Class<?> paramType) {
        if (value == null) {
            if (Boolean.TYPE.equals(paramType)) {
                return Boolean.FALSE;
            } else if (paramType.isPrimitive()) {
                if (boolean.class.isAssignableFrom(paramType)) {
                    return false;
                }
                if (long.class.isAssignableFrom(paramType)
                        || int.class.isAssignableFrom(paramType)
                        || short.class.isAssignableFrom(paramType)
                        || float.class.isAssignableFrom(paramType)
                        || double.class.isAssignableFrom(paramType)
                        || byte.class.isAssignableFrom(paramType)) {
                    return 0;
                }
                throw new IllegalStateException("Optional" + paramType.getSimpleName() + " parameter '" + name +
                        "' is present but cannot be translated into a null value due to being declared as a " +
                        "primitive type. consider declaring it sa object wrapper for the corresponding primitive type.");
            }
        }
        return value;
    }

    protected Object adaptArgumentIfNecessary(@Nullable Object arg, MethodParameter parameter) {
        if (parameter.getParameterType() == Optional.class) {
            if (arg == null || (arg instanceof Collection && ((Collection) arg).isEmpty())
                    || (arg instanceof Object[] && ((Object[]) arg).length == 0)) {
                return Optional.empty();
            } else {
                return Optional.of(arg);
            }
        }
        return arg;
    }
}
