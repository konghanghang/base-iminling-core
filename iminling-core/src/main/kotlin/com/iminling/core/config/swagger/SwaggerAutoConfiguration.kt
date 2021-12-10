package com.iminling.core.config.swagger

import com.fasterxml.classmate.TypeResolver
import com.iminling.core.properties.Knife4jApiInfoProperties
import io.swagger.annotations.ApiOperation
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.schema.TypeNameExtractor
import springfox.documentation.schema.plugins.SchemaPluginsManager
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.EnumTypeDeterminer
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager

/**
 * @author  yslao@outlook.com
 * @since  2021/12/8
 */
@EnableConfigurationProperties(Knife4jApiInfoProperties::class)
class SwaggerAutoConfiguration {

    /**
     * knife4j配置
     */
    @Bean(value = ["defaultApi2"])
    @ConditionalOnMissingBean(Docket::class)
    fun defaultApi2(knife4jApiInfoProperties: Knife4jApiInfoProperties): Docket {
        /*var selectors = mutableListOf<Predicate<RequestHandler>>()
        knife4jApiInfoProperties.packages.forEach { selectors.add(RequestHandlerSelectors.basePackage(it)) }*/
        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(
                ApiInfoBuilder() //.title("swagger-bootstrap-ui-demo RESTful APIs")
                    .description(knife4jApiInfoProperties.description)
                    .termsOfServiceUrl(knife4jApiInfoProperties.serviceUrl)
                    .contact(
                        Contact(
                            knife4jApiInfoProperties.contactName,
                            knife4jApiInfoProperties.contactUrl,
                            knife4jApiInfoProperties.contactEmail
                        )
                    )
                    .version(knife4jApiInfoProperties.version)
                    .build()
            ) //分组名称
            .groupName(knife4jApiInfoProperties.groupName)
            .enable(knife4jApiInfoProperties.enable)
            .select() //这里指定Controller扫描包路径
            .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation::class.java))
            .paths(PathSelectors.any())
            .build()
    }

    @Bean
    fun customizeParameterBuilderPlugin(): CustomizeParameterBuilderPlugin {
        return CustomizeParameterBuilderPlugin()
    }

    @Bean
    fun customizeOperationModelsProviderPlugin(
        pluginsManager: SchemaPluginsManager,
        resolver: TypeResolver
    ): CustomizeOperationModelsProviderPlugin {
        return CustomizeOperationModelsProviderPlugin(pluginsManager, resolver)
    }

    @Bean
    fun customizeOperationBuilderPlugin(
        pluginsManager: SchemaPluginsManager,
        enumTypeDeterminer: EnumTypeDeterminer,
        nameExtractor: TypeNameExtractor,
        documentationPluginsManager: DocumentationPluginsManager,
        resolver: TypeResolver
    ): CustomizeOperationBuilderPlugin {
        return CustomizeOperationBuilderPlugin(
            pluginsManager,
            enumTypeDeterminer,
            nameExtractor,
            documentationPluginsManager,
            resolver
        )
    }

    @Bean
    fun customizeModelPropertyBuilderPlugin(): CustomizeModelPropertyBuilderPlugin {
        return CustomizeModelPropertyBuilderPlugin()
    }

}