package com.iminling.core.config.filter

import com.iminling.core.exception.BizException
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.web.method.HandlerMethod
import javax.servlet.http.HttpServletRequest

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
// @JvmDefaultWithoutCompatibility
interface Filter: Ordered {

    @Throws(BizException::class)
    fun doFilter(handlerMethod: HandlerMethod, request: HttpServletRequest)

    fun <T : Annotation> getAnnotation(handlerMethod: HandlerMethod, clazz: Class<T>): T? {
        return AnnotationUtils.findAnnotation(handlerMethod.method, clazz)
            ?: handlerMethod.beanType.getAnnotation(clazz)
    }

}