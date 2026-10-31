package com.iminling.core.config.value;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.constant.MessageCode;
import com.iminling.core.constant.ResolveStrategy;

import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.*;

@RestControllerAdvice
public class GlobalReturnValueHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(
            MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converter) {
        Class<?> type = returnType.getParameterType();
        if (ResultModel.class.isAssignableFrom(type)
                || ResponseEntity.class.isAssignableFrom(type)
                || StreamingResponseBody.class.isAssignableFrom(type)
                || SseEmitter.class.isAssignableFrom(type)) return false;
        EnableResolve resolve = returnType.getMethodAnnotation(EnableResolve.class);
        if (resolve == null)
            resolve = returnType.getContainingClass().getAnnotation(EnableResolve.class);
        return resolve != null
                && (resolve.value() == ResolveStrategy.ALL
                        || resolve.value() == ResolveStrategy.RETURN_VALUE);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType contentType,
            Class<? extends HttpMessageConverter<?>> converter,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof ResultModel<?>) return body;
        ResultModel<Object> result = new ResultModel<>(MessageCode.RESULT_OK);
        result.setData(body);
        if (returnType.getParameterType() == String.class) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return JsonUtil.getInstant().writeValueAsString(result);
        }
        return result;
    }
}
