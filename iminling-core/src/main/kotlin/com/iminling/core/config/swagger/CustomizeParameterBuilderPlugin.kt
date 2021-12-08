package com.iminling.core.config.swagger

import com.google.common.collect.Lists
import com.iminling.core.annotation.EnableResolve
import com.iminling.core.constant.ResolveStrategy
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.ParameterBuilderPlugin
import springfox.documentation.spi.service.contexts.OperationContext
import springfox.documentation.spi.service.contexts.ParameterContext
import java.util.*

/**
 * @author  yslao@outlook.com
 * @since  2021/12/8
 */
class CustomizeParameterBuilderPlugin: ParameterBuilderPlugin {

    private val methods: List<String> = Lists.newArrayList("post", "put", "patch")

    override fun apply(context: ParameterContext) {
        val operationContext: OperationContext = context.operationContext
        var enableResolveOptional = operationContext.findAnnotation(
            EnableResolve::class.java
        )
        if (!enableResolveOptional.isPresent) {
            enableResolveOptional = operationContext.findControllerAnnotation(EnableResolve::class.java)
        }
        if (enableResolveOptional.isPresent) {
            val enableResolve = enableResolveOptional.get()
            val strategy = enableResolve.value
            if (strategy == ResolveStrategy.ARGUMENTS || strategy == ResolveStrategy.ALL) {
                // 设置参数类型为body
                val httpMethodName = operationContext.httpMethod().name.lowercase(Locale.getDefault())
                if (methods.contains(httpMethodName)) {
                    context.parameterBuilder().parameterType("body")
                }
            }
        }
    }

    override fun supports(delimiter: DocumentationType): Boolean {
        return true
    }
}