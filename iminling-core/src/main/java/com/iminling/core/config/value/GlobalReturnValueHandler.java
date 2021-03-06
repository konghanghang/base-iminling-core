package com.iminling.core.config.value;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.annotation.EnableResolve;
import com.iminling.model.common.MessageCode;
import com.iminling.model.common.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

@Slf4j
@RestControllerAdvice
public class GlobalReturnValueHandler implements ResponseBodyAdvice<Object> {

    private final ConfigurableEnvironment environment;
    private final boolean enableResultLog;

    public GlobalReturnValueHandler(ConfigurableEnvironment environment) {
        this.environment = environment;
        enableResultLog =  environment.getProperty("application.log.result", Boolean.class, false);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> parameterType = returnType.getParameterType();
        if (ResultModel.class.isAssignableFrom(parameterType)
                || ResponseEntity.class.isAssignableFrom(parameterType)
                || StreamingResponseBody.class.isAssignableFrom(parameterType)
                || StreamingResponseBody.class.isAssignableFrom(parameterType)
                || SseEmitter.class.isAssignableFrom(parameterType)) {
            return false;
        }
        Method method = returnType.getMethod();
        AnnotatedElement annotatedElement = returnType.getAnnotatedElement();
        EnableResolve enableResolve = annotatedElement.getAnnotation(EnableResolve.class);
        if (enableResolve == null) {
            if (method != null) enableResolve = method.getAnnotation(EnableResolve.class);
        }
        if (enableResolve == null) {
            enableResolve =  returnType.getContainingClass().getAnnotation(EnableResolve.class);
        }
        if (enableResolve == null) return false;
        EnableResolve.ResolveStrategy value = enableResolve.value();
        return value == EnableResolve.ResolveStrategy.ALL || value == EnableResolve.ResolveStrategy.RETURN_VALUE;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ResultModel resultModel = new ResultModel(MessageCode.RESULT_OK);
        /*if (isPrimitiveOrVoid(returnType.getParameterType())) {
            BaseMap json = new BaseMap("result", body);
            body = json;
        }*/
        resultModel.setData(body);
        boolean isString = returnType.getParameterType() == String.class;
        if (isString) {
            String result = JsonUtil.obj2Str(resultModel);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            if (enableResultLog)
                log.info("url:{}, 返回结果：{}", request.getURI(), result);
            return result;
        }
        if (enableResultLog)
            log.info("url:{}, 返回结果：{}", request.getURI(), resultModel);
        return resultModel;
    }

    private boolean isPrimitiveOrVoid(Class<?> returnType) {
        return returnType.isPrimitive()
                || Number.class.isAssignableFrom(returnType)
                || CharSequence.class.isAssignableFrom(returnType)
                || Character.class.isAssignableFrom(returnType)
                || Boolean.class.isAssignableFrom(returnType);
    }

}
