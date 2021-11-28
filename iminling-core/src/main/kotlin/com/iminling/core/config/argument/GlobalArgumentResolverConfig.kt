package com.iminling.core.config.argument

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

/**
 * 自定义的参数处理器放在第一位
 * 功能和GlobalArgumentBeanPostProcessor一样，只生效一个就行，所以注释了 @Configuration
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
// @Configuration
class GlobalArgumentResolverConfig(
    private val requestMappingHandlerAdapter: RequestMappingHandlerAdapter,
    private val objectMapper: ObjectMapper
) {

    init {
        val argumentResolvers: MutableList<HandlerMethodArgumentResolver> = ArrayList()
        argumentResolvers.add(GlobalArgumentResolver(objectMapper))
        argumentResolvers.addAll(requestMappingHandlerAdapter.argumentResolvers!!)
        requestMappingHandlerAdapter.argumentResolvers = argumentResolvers
    }

}