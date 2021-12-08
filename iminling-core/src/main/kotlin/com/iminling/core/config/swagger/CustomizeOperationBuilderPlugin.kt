package com.iminling.core.config.swagger

import com.fasterxml.classmate.ResolvedType
import com.fasterxml.classmate.TypeResolver
import com.google.common.base.CaseFormat
import com.google.common.collect.Lists
import com.iminling.core.annotation.EnableResolve
import com.iminling.core.config.value.ResultModel
import com.iminling.core.constant.ResolveStrategy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.schema.*
import springfox.documentation.schema.plugins.SchemaPluginsManager
import springfox.documentation.service.Parameter
import springfox.documentation.service.ResolvedMethodParameter
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.EnumTypeDeterminer
import springfox.documentation.spi.schema.contexts.ModelContext
import springfox.documentation.spi.service.OperationBuilderPlugin
import springfox.documentation.spi.service.contexts.OperationContext
import springfox.documentation.spi.service.contexts.ParameterContext
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager
import springfox.documentation.spring.web.readers.operation.ResponseMessagesReader
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * @author  yslao@outlook.com
 * @since  2021/12/8
 */
class CustomizeOperationBuilderPlugin(
    private val pluginsManager: SchemaPluginsManager,
    private val enumTypeDeterminer: EnumTypeDeterminer,
    private val nameExtractor: TypeNameExtractor,
    private val documentationPluginsManager: DocumentationPluginsManager,
    private val resolver: TypeResolver
) : OperationBuilderPlugin {

    private val log = LoggerFactory.getLogger(OperationBuilderPlugin::class.java)

    private val methods: List<String> = Lists.newArrayList("post", "put", "patch")

    override fun apply(context: OperationContext) {
        var enableResolveOptional = context.findAnnotation(
            EnableResolve::class.java
        )
        if (!enableResolveOptional.isPresent) {
            enableResolveOptional = context.findControllerAnnotation(EnableResolve::class.java)
        }
        if (enableResolveOptional.isPresent) {
            val enableResolve = enableResolveOptional.get()
            val strategy = enableResolve.value
            // 处理返回值
            if (strategy == ResolveStrategy.RETURN_VALUE || strategy == ResolveStrategy.ALL) {
                var returnType = resolver.resolve(ResultModel::class.java, context.returnType)
                returnType = context.alternateFor(returnType)
                val viewProvider = pluginsManager.viewProvider(context.documentationContext.documentationType)
                val modelContext = context.operationModelsBuilder().addReturn(
                    returnType,
                    viewProvider.viewFor(returnType, context)
                )
                val knownNames: MutableMap<String, String> = HashMap()
                Optional.ofNullable(context.knownModels[modelContext.parameterId])
                    .orElse(HashSet())
                    .forEach(Consumer { model: Model ->
                        knownNames[model.id] = model.name
                    })
                val responseTypeName = nameExtractor.typeName(modelContext)
                log.debug(
                    "Setting spring response class to: {}",
                    responseTypeName
                )
                context.operationBuilder().responseModel(
                    ResolvedTypes.modelRefFactory(modelContext, enumTypeDeterminer, nameExtractor, knownNames)
                        .apply(returnType)
                )

                // 处理ResponseMessages 200状态的情况
                val responseMessages = context.getGlobalResponseMessages(context.httpMethod().toString())
                context.operationBuilder().responseMessages(HashSet(responseMessages))
                applyReturnTypeOverride(context, returnType, modelContext)
            }
            // 处理参数
            if (strategy == ResolveStrategy.ARGUMENTS || strategy == ResolveStrategy.ALL) {
                val httpMethodName = context.httpMethod().name.lowercase(Locale.getDefault())
                if (methods.contains(httpMethodName)) {
                    val operationBuilder = context.operationBuilder()
                    try {
                        val parameters = operationBuilder.javaClass.getDeclaredField("parameters")
                        parameters.isAccessible = true
                        parameters[operationBuilder] = ArrayList<Any>()
                    } catch (e: NoSuchFieldException) {
                        log.error(
                            "CustOperationResponseClassReader error",
                            e
                        )
                    } catch (e: IllegalAccessException) {
                        log.error(
                            "CustOperationResponseClassReader error",
                            e
                        )
                    }
                    operationBuilder.parameters(readParameters(context))
                }
            }
        }
    }

    override fun supports(delimiter: DocumentationType): Boolean {
        return true
    }

    /**
     * 参考springfox.documentation.spring.web.readers.operation.ResponseMessagesReader
     * @param context
     * @param returnType
     * @param modelContext
     */
    private fun applyReturnTypeOverride(
        context: OperationContext,
        returnType: ResolvedType,
        modelContext: ModelContext
    ) {
        val httpStatusCode = ResponseMessagesReader.httpStatusCode(context)
        val message = ResponseMessagesReader.message(context)
        var modelRef: ModelReference? = null
        if (!Types.isVoid(returnType)) {
            val knownNames: MutableMap<String, String> = HashMap()
            Optional.ofNullable(context.knownModels[modelContext.parameterId])
                .orElse(HashSet())
                .forEach(Consumer { model: Model ->
                    knownNames[model.id] = model.name
                })
            modelRef = ResolvedTypes.modelRefFactory(
                modelContext,
                enumTypeDeterminer,
                nameExtractor,
                knownNames
            ).apply(returnType)
        }
        val built = ResponseMessageBuilder().code(httpStatusCode).message(message).responseModel(modelRef)
            .build()
        context.operationBuilder().responseMessages(setOf(built))
    }

    /**
     * springfox.documentation.spring.web.readers.operation.OperationParameterReader
     * @param context
     * @return
     */
    private fun readParameters(context: OperationContext): List<Parameter>? {
        val parameters: MutableList<Parameter> = ArrayList()
        try {
            val className =
                CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, context.groupName) + CaseFormat.LOWER_CAMEL.to(
                    CaseFormat.UPPER_CAMEL,
                    context.name
                )
            val aClass = Class.forName("com.iminling.javassist.model.$className")
            val resolvedType = resolver.resolve(aClass)
            val bodyParameter = ResolvedMethodParameter(0, "json", ArrayList(), resolvedType)
            val parameterContext = ParameterContext(
                bodyParameter,
                ParameterBuilder(),
                context.documentationContext,
                context.genericsNamingStrategy,
                context
            )
            parameters.add(documentationPluginsManager.parameter(parameterContext))
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return parameters.stream().filter(Predicate { obj: Parameter -> obj.isHidden }
            .negate()).collect(Collectors.toList())
    }
}