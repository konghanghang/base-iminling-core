package com.iminling.core.config.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
public class MybatisConfig implements ApplicationContextAware {

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    private ApplicationContext applicationContext;
    private final MybatisPlusProperties mybatisPlusProperties;

    public MybatisConfig(MybatisPlusProperties mybatisPlusProperties) {
        this.mybatisPlusProperties = mybatisPlusProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * mybatis-plus分页插件
     * @return PaginationInnerInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * map接收,转驼峰
     * @return ConfigurationCustomizer
     */
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer(){
        return configuration -> configuration.setObjectWrapperFactory(new MapWrapperFactory());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mybatis-plus", name = "refresh-mapper", havingValue = "true")
    public MybatisMapperRefresh mybatisMapperRefresh(ConfigurableEnvironment environment){
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) applicationContext.getBean("sqlSessionFactory");
        // Resource[] resources = new Resource[0];
        String[] mapperLocations = mybatisPlusProperties.getMapperLocations();
        List<Resource> resources = new ArrayList<>();
        for (String mapperLocation : mapperLocations) {
            try {
                resources.addAll(Arrays.asList(resourceResolver.getResources(mapperLocation)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Integer interval = environment.getProperty("mybatis-plus.refresh-mapper-interval", Integer.class, 5);
        MybatisMapperRefresh mybatisMapperRefresh = new MybatisMapperRefresh(resources.toArray(new Resource[]{}), sqlSessionFactory, interval);
        return mybatisMapperRefresh;

    }

}
