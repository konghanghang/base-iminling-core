package com.iminling.core.config.argument

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.iminling.common.json.JsonUtil
import com.iminling.core.annotation.EnableResolve
import com.iminling.core.constant.ResolveStrategy
import com.iminling.core.constant.StringEnum
import org.springframework.core.GenericTypeResolver
import org.springframework.core.MethodParameter
import org.springframework.util.Assert
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.*
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.lang.reflect.Type
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 自定义参数处理器
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
class GlobalArgumentResolver(private val objectMapper: ObjectMapper) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)!!.request
        if (!supportsParameter(request, parameter)) {
            return false
        }
        var enableResolve =
            parameter.getMethodAnnotation(EnableResolve::class.java) ?: parameter.containingClass.getAnnotation(
                EnableResolve::class.java
            )
        if (Objects.isNull(enableResolve)) {
            return false
        }
        val value = enableResolve!!.value
        return value == ResolveStrategy.ALL || value == ResolveStrategy.ARGUMENTS
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        Assert.state(mavContainer != null, "ModelAttributeMethodProcessor requires ModelAndViewContainer")
        Assert.state(binderFactory != null, "ModelAttributeMethodProcessor requires WebDataBinderFactory")

        val requestDataWrapper = getRequestData(webRequest)
        if (requestDataWrapper == null) {
            var path: String? = null
            if (webRequest is ServletWebRequest) {
                path = webRequest.request.servletPath
            }
            throw IllegalArgumentException(
                "无法注入'${parameter.parameterName}'参数， 当前不是JSON请求， path = ${path ?: parameter.method}"
            )
        }
        var nestedParameter = parameter.nestedIfOptional()
        var arg = readWithRequestData(requestDataWrapper, nestedParameter, nestedParameter.nestedGenericParameterType)
        arg = handleNullValue(nestedParameter.parameterName!!, arg, nestedParameter.parameterType)
        return adaptArgumentIfNecessary(arg, nestedParameter)
    }

    private fun supportsParameter(request: HttpServletRequest, parameter: MethodParameter): Boolean {
        val parameterType = parameter.parameterType
        if (HttpServletResponse::class.java.isAssignableFrom(parameterType)
            || HttpServletRequest::class.java.isAssignableFrom(parameterType)
        ) {
            return false
        }
        // 特殊处理@PathVariable注解
        val pathVariable = parameter.getParameterAnnotation(PathVariable::class.java)
        if (Objects.nonNull(pathVariable)) {
            return false
        }
        // 特殊处理@RequestHeader注解
        val requestHeader = parameter.getParameterAnnotation(RequestHeader::class.java)
        if (Objects.nonNull(requestHeader)) {
            return false
        }
        val requestDataWrapper = request.getAttribute(StringEnum.REQUEST_DATA_KEY.desc) as RequestDataWrapper?
        return Objects.nonNull(requestDataWrapper) && requestDataWrapper!!.canRead
    }

    private fun getRequestData(webRequest: NativeWebRequest?): RequestDataWrapper? {
        return if (webRequest == null) {
            null
        } else webRequest.getAttribute(
            StringEnum.REQUEST_DATA_KEY.desc,
            RequestAttributes.SCOPE_REQUEST
        ) as RequestDataWrapper?
    }

    @Throws(Exception::class)
    private fun readWithRequestData(
        requestDataWrapper: RequestDataWrapper,
        parameter: MethodParameter,
        parameterType: Type?
    ): Any? {
        var result: Any? = null
        if (requestDataWrapper.hasParams) {
            val params = requestDataWrapper.params
            val parameterName = parameter.parameterName
            if (params!!.hasNonNull(parameterName)) {
                val javaType = getJavaType(parameterType, null)
                val value = params.path(parameterName)
                // 这里可能还需要捕捉异常
                val obj: Any = objectMapper.convertValue(value, javaType)
                // 判断对象里边的key是不是都为null
                if (JsonUtil.objIsNull(obj)) {
                    return null
                }
                result = obj
            }
        }
        return result
    }

    private fun getJavaType(type: Type?, contextClass: Class<*>?): JavaType? {
        val typeFactory = objectMapper.typeFactory
        return typeFactory.constructType(GenericTypeResolver.resolveType(type!!, contextClass))
    }

    private fun handleNullValue(name: String, value: Any?, paramType: Class<*>): Any? {
        if (value == null) {
            if (java.lang.Boolean.TYPE == paramType) {
                return java.lang.Boolean.FALSE
            } else if (paramType.isPrimitive) {
                if (Boolean::class.javaPrimitiveType!!.isAssignableFrom(paramType)) {
                    return false
                }
                if (Long::class.javaPrimitiveType!!.isAssignableFrom(paramType)
                    || Int::class.javaPrimitiveType!!.isAssignableFrom(paramType)
                    || Short::class.javaPrimitiveType!!.isAssignableFrom(paramType)
                    || Float::class.javaPrimitiveType!!.isAssignableFrom(paramType)
                    || Double::class.javaPrimitiveType!!.isAssignableFrom(paramType)
                    || Byte::class.javaPrimitiveType!!.isAssignableFrom(paramType)
                ) {
                    return 0
                }
                throw IllegalStateException(
                    "Optional ${paramType.simpleName} parameter '$name' is present but cannot be translated into a null value due to being declared as a " +
                            "primitive type. consider declaring it sa object wrapper for the corresponding primitive type."
                )
            }
        }
        return value
    }

    private fun adaptArgumentIfNecessary(arg: Any?, parameter: MethodParameter): Any? {
        return if (parameter.parameterType == Optional::class.java) {
            if (arg == null || (arg is Collection<*> && arg.isEmpty())
                || (arg is Array<*> && arg.size == 0)
            ) {
                Optional.empty()
            } else {
                Optional.of(arg)
            }
        } else arg
    }
}