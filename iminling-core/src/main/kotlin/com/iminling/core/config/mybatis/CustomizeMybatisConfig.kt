package com.iminling.core.config.mybatis

import com.baomidou.mybatisplus.annotation.DbType
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
import org.apache.ibatis.session.SqlSessionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import java.io.IOException

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
@ConditionalOnClass(MybatisPlusAutoConfiguration::class)
class CustomizeMybatisConfig(var mybatisPlusProperties: MybatisPlusProperties): ApplicationContextAware {

    private val resourceResolver: ResourcePatternResolver = PathMatchingResourcePatternResolver()

    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * mybatis-plus分页插件
     * @return PaginationInnerInterceptor
     */
    @Bean
    fun mybatisPlusInterceptor(): MybatisPlusInterceptor? {
        val interceptor = MybatisPlusInterceptor()
        interceptor.addInnerInterceptor(PaginationInnerInterceptor(DbType.MYSQL))
        return interceptor
    }

    /**
     * 自定义参数填充
     * @return CustomizeMetaObjectHandler
     */
    @Bean
    fun customizeMetaObjectHandler(): CustomizeMetaObjectHandler {
        return CustomizeMetaObjectHandler()
    }

    /**
     * map接收,转驼峰
     * @return ConfigurationCustomizer
     */
    @Bean
    fun mybatisConfigurationCustomizer(): ConfigurationCustomizer {
        return ConfigurationCustomizer { configuration: MybatisConfiguration ->
            configuration.objectWrapperFactory = MapWrapperFactory()
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "mybatis-plus", name = ["refresh-mapper"], havingValue = "true")
    fun mybatisMapperRefresh(environment: ConfigurableEnvironment): MybatisMapperRefresh {
        val sqlSessionFactory = applicationContext.getBean("sqlSessionFactory") as SqlSessionFactory
        // Resource[] resources = new Resource[0];
        val mapperLocations = mybatisPlusProperties.mapperLocations
        val resources: MutableList<Resource> = mutableListOf()
        for (mapperLocation in mapperLocations) {
            try {
                resources.addAll(resourceResolver.getResources(mapperLocation).toList())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val interval = environment.getProperty("mybatis-plus.refresh-mapper-interval", Int::class.java, 5)
        return MybatisMapperRefresh(resources.toTypedArray(), sqlSessionFactory, interval)
    }

}