package com.iminling.core.config.argument

import com.iminling.common.json.JsonUtil
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

/**
 * 添加GlobalArgumentResolver到RequestMappingHandlerAdapter
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
class GlobalArgumentBeanPostProcessor: BeanPostProcessor {

    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is RequestMappingHandlerAdapter) {
            val argumentResolvers: MutableList<HandlerMethodArgumentResolver> = ArrayList()
            argumentResolvers.add(GlobalArgumentResolver(JsonUtil.getInstant()))
            argumentResolvers.addAll(bean.argumentResolvers!!)
            bean.argumentResolvers = argumentResolvers
        }
        return bean
    }

}