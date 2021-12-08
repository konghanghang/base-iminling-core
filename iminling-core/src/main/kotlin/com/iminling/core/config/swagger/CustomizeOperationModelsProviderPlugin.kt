package com.iminling.core.config.swagger

import com.fasterxml.classmate.ResolvedType
import com.fasterxml.classmate.TypeResolver
import com.google.common.base.CaseFormat
import com.iminling.core.annotation.EnableResolve
import com.iminling.core.config.value.ResultModel
import com.iminling.core.constant.ResolveStrategy
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiModelProperty
import javassist.*
import javassist.bytecode.AccessFlag
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.StringMemberValue
import springfox.documentation.schema.plugins.SchemaPluginsManager
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.OperationModelsProviderPlugin
import springfox.documentation.spi.service.contexts.RequestMappingContext
import java.util.*

/**
 * @author  yslao@outlook.com
 * @since  2021/12/8
 */
class CustomizeOperationModelsProviderPlugin(
    private val pluginsManager: SchemaPluginsManager,
    private val resolver: TypeResolver
) :
    OperationModelsProviderPlugin {

    override fun apply(context: RequestMappingContext) {
        // 获取方法和类上的所有EnableResolve注解

        // 获取方法和类上的所有EnableResolve注解
        val annotations = context.findAnnotations(EnableResolve::class.java)
        var enableResolve: EnableResolve? = null
        if (annotations.size > 0) {
            // 获取方法上的EnableResolve注解
            val optionalEnableResolve = context.findAnnotation(EnableResolve::class.java)
            enableResolve = if (optionalEnableResolve.isPresent) {
                optionalEnableResolve.get()
            } else {
                annotations[0]
            }
        }
        if (enableResolve != null) {
            val strategy = enableResolve.value
            if (strategy == ResolveStrategy.ARGUMENTS || strategy == ResolveStrategy.ALL) {
                val methodParameters = context.parameters
                val classPool = ClassPool.getDefault()
                //创建类名
                val className =
                    CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, context.groupName) + CaseFormat.LOWER_CAMEL.to(
                        CaseFormat.UPPER_CAMEL,
                        context.name
                    )
                val ctClass = classPool.makeClass("com.iminling.javassist.model.$className")
                for (methodParameter in methodParameters) {
                    val fieldNameOptional = methodParameter.defaultName()
                    val typeName = methodParameter.parameterType.typeName
                    try {
                        var fieldName = "unknown"
                        if (fieldNameOptional.isPresent) {
                            fieldName = fieldNameOptional.get()
                        }
                        val ctField = CtField(classPool[typeName], fieldName, ctClass)
                        val upperName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName!!)
                        ctClass.addMethod(CtNewMethod.setter("set$upperName", ctField))
                        ctClass.addMethod(CtNewMethod.getter("get$upperName", ctField))
                        // 添加注解
                        addAnnotation(context, ctClass, ctField)
                        ctField.fieldInfo.accessFlags = AccessFlag.PRIVATE
                        ctClass.addField(ctField)
                    } catch (e: CannotCompileException) {
                        e.printStackTrace()
                    } catch (e: NotFoundException) {
                        e.printStackTrace()
                    }
                }
                try {
                    val resolve = resolver.resolve(ctClass.toClass())
                    context.operationModelsBuilder().addReturn(
                        resolve,
                        viewForReturn(context, resolve)
                    )
                } catch (e: CannotCompileException) {
                    e.printStackTrace()
                }
            }
        }
        var modelType = resolver.resolve(ResultModel::class.java, context.returnType)
        modelType = context.alternateFor(modelType)
        context.operationModelsBuilder().addReturn(
            modelType,
            viewForReturn(context, modelType)
        )
    }

    override fun supports(delimiter: DocumentationType): Boolean {
        return true
    }

    private fun viewForReturn(
        context: RequestMappingContext,
        regularModel: ResolvedType
    ): Optional<ResolvedType?> {
        val viewProvider = pluginsManager.viewProvider(context.documentationContext.documentationType)
        return viewProvider.viewFor(
            regularModel,
            context
        )
    }

    private fun addAnnotation(context: RequestMappingContext, ctClass: CtClass, ctField: CtField) {
        val implicitParamOptional = context.findAnnotation(
            ApiImplicitParam::class.java
        )
        if (implicitParamOptional.isPresent) {
            // 就一个参数
            val apiImplicitParam = implicitParamOptional.get()
            ctField.fieldInfo.addAttribute(getAnnotationsAttribute(ctClass, apiImplicitParam.value))
        } else {
            context.findAnnotation(ApiImplicitParams::class.java).ifPresent { implicitParams ->
                Arrays.stream(implicitParams.value)
                    .filter { apiImplicitParam -> ctField.name == apiImplicitParam.name }
                    .findFirst()
                    .ifPresent { param ->
                        ctField.fieldInfo.addAttribute(getAnnotationsAttribute(ctClass, param.value))
                    }
            }
        }
    }

    private fun getAnnotationsAttribute(ctClass: CtClass, value: String): AnnotationsAttribute {
        val constPool = ctClass.classFile.constPool
        val attr = AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag)
        val ann = Annotation(ApiModelProperty::class.java.name, constPool)
        ann.addMemberValue("value", StringMemberValue(value, constPool))
        attr.addAnnotation(ann)
        return attr
    }
}