package com.iminling.core.config.feign

import feign.Feign
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * feign相关配置
 * @author yslao@outlook.com
 * @since 2022/5/20
 */
@ConditionalOnClass(Feign::class)
@ConditionalOnProperty(prefix = "app.customize", name = ["feign"], havingValue = "true", matchIfMissing = true)
class CustomizeFeignAutoConfiguration {

    /**
     * 处理：@FeignClient中加在类上的@RequestMapping也被SpringMVC加载的问题解决
     * @return
     */
    @Bean
    fun feignWebRegistrations(): WebMvcRegistrations {
        return object : WebMvcRegistrations {
            override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping {
                return FeignRequestMappingHandlerMapping()
            }
        }
    }

    private class FeignRequestMappingHandlerMapping : RequestMappingHandlerMapping() {
        override fun isHandler(beanType: Class<*>): Boolean {
            return super.isHandler(beanType) &&
                    !AnnotatedElementUtils.isAnnotated(beanType, FeignClient::class.java)
        }
    }

}