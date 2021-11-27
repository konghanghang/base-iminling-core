package com.iminling.core.config.value

import com.iminling.common.json.JsonUtil
import com.iminling.core.annotation.EnableResolve
import com.iminling.core.annotation.EnableResolve.ResolveStrategy
import com.iminling.core.constant.MessageCode
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
@RestControllerAdvice
class GlobalReturnValueHandler: ResponseBodyAdvice<Any> {
    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        val parameterType = returnType.parameterType
        if (ResultModel::class.java.isAssignableFrom(parameterType)
            || ResponseEntity::class.java.isAssignableFrom(parameterType)
            || StreamingResponseBody::class.java.isAssignableFrom(parameterType)
            || StreamingResponseBody::class.java.isAssignableFrom(parameterType)
            || SseEmitter::class.java.isAssignableFrom(parameterType)
        ) {
            return false
        }
        val method = returnType.method
        val annotatedElement = returnType.annotatedElement
        var enableResolve = annotatedElement.getAnnotation(EnableResolve::class.java)
        if (enableResolve == null) {
            if (method != null) enableResolve = method.getAnnotation(EnableResolve::class.java)
        }
        if (enableResolve == null) {
            enableResolve = returnType.containingClass.getAnnotation(EnableResolve::class.java)
        }
        if (enableResolve == null) return false
        val value = enableResolve.value
        return value == ResolveStrategy.ALL || value == ResolveStrategy.RETURN_VALUE
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any {
        val resultModel = ResultModel<Any>(MessageCode.RESULT_OK)
        resultModel.data = body
        val isString = returnType.parameterType == String::class.java
        if (isString) {
            val result = JsonUtil.obj2Str(resultModel)
            response.headers.contentType = MediaType.APPLICATION_JSON
            return result
        }
        return resultModel
    }

    private fun isPrimitiveOrVoid(returnType: Class<*>): Boolean {
        return (returnType.isPrimitive
                || Number::class.java.isAssignableFrom(returnType)
                || CharSequence::class.java.isAssignableFrom(returnType)
                || Char::class.java.isAssignableFrom(returnType)
                || Boolean::class.java.isAssignableFrom(returnType))
    }
}