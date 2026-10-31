package com.iminling.core.config.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;

@AutoConfiguration(before = MybatisPlusAutoConfiguration.class)
@ConditionalOnClass({MybatisPlusAutoConfiguration.class, PaginationInnerInterceptor.class})
public class CustomizeMybatisConfig {
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean(com.baomidou.mybatisplus.core.handlers.MetaObjectHandler.class)
    public CustomizeMetaObjectHandler customizeMetaObjectHandler() {
        return new CustomizeMetaObjectHandler();
    }

    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return config -> config.setObjectWrapperFactory(new MapWrapperFactory());
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "mybatis",
            name = "sql.log",
            havingValue = "true",
            matchIfMissing = true)
    public SqlInterceptor sqlInterceptor() {
        return new SqlInterceptor();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "mybatis-plus", name = "refresh-mapper", havingValue = "true")
    public MybatisMapperRefresh mybatisMapperRefresh(
            MybatisPlusProperties properties, SqlSessionFactory factory, Environment environment)
            throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = new ArrayList<Resource>();
        for (String pattern : properties.getMapperLocations())
            resources.addAll(java.util.List.of(resolver.getResources(pattern)));
        return new MybatisMapperRefresh(
                resources.toArray(Resource[]::new),
                factory,
                environment.getProperty("mybatis-plus.refresh-mapper-interval", Integer.class, 5));
    }
}
