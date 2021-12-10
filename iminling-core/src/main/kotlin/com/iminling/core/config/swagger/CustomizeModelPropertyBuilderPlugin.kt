package com.iminling.core.config.swagger

import com.fasterxml.jackson.annotation.JsonValue
import com.google.common.base.Joiner
import com.iminling.core.annotation.swagger.SwaggerDisplayEnum
import io.swagger.annotations.ApiModelProperty
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.ReflectionUtils
import springfox.documentation.schema.Annotations
import springfox.documentation.service.AllowableListValues
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin
import springfox.documentation.spi.schema.contexts.ModelPropertyContext

/**
 * @author  yslao@outlook.com
 * @since  2021/12/10
 */
class CustomizeModelPropertyBuilderPlugin: ModelPropertyBuilderPlugin {

    override fun apply(context: ModelPropertyContext) {
        var propertyDefinitionOptional = context.beanPropertyDefinition
        if (!propertyDefinitionOptional.isPresent) {
            return
        }
        var propertyDefinition = propertyDefinitionOptional.get()
        var rawType = propertyDefinition.field.rawType
        if (!Enum::class.java.isAssignableFrom(rawType)) {
            return
        }
        var swaggerDisplayEnum =
            AnnotationUtils.findAnnotation(rawType, SwaggerDisplayEnum::class.java) ?: return
        var code = swaggerDisplayEnum.code
        var desc = swaggerDisplayEnum.desc

        val allowableValues = mutableListOf<String>()
        var clazz: Class<*>? = null
        var displayValues = rawType.enumConstants.filterNotNull().map {
            var javaClass = it.javaClass
            var codeField = ReflectionUtils.findField(javaClass, code)
            ReflectionUtils.makeAccessible(codeField!!)
            var codeValue = ReflectionUtils.getField(codeField, it)

            var descField = ReflectionUtils.findField(javaClass, desc)
            ReflectionUtils.makeAccessible(descField!!)
            var descValue = ReflectionUtils.getField(descField, it)

            if (codeField.getAnnotation(JsonValue::class.java) != null) {
                allowableValues.add(codeValue.toString())
                clazz = codeField.type
            } else if (descField.getAnnotation(JsonValue::class.java) != null) {
                allowableValues.add(descValue.toString())
                clazz = descField.type
            } else {
                allowableValues.add(codeValue.toString())
                clazz = codeField.type
            }
            "$codeValue:$descValue"
        }

        var builder = context.builder
        var displayName = "enum"
        var apiModelPropertyOptional =
            Annotations.findPropertyAnnotation(propertyDefinition, ApiModelProperty::class.java)
        if (apiModelPropertyOptional.isPresent) {
            var apiModelProperty = apiModelPropertyOptional.get()
            displayName = apiModelProperty.value
        }
        val joinText = displayName + " (" + Joiner.on(";").join(displayValues) + ")"
        builder.description(joinText).type(context.resolver.resolve(rawType))
        val values = AllowableListValues(allowableValues, "LIST")
        builder.allowableValues(values)
        builder.type(context.resolver.resolve(clazz))
    }

    override fun supports(delimiter: DocumentationType): Boolean {
        return true
    }
}