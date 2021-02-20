package com.iminling.core.config.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.MySqlDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    /**
     * mybatis-plus分页插件
     * @return PaginationInnerInterceptor
     */
    @Bean
    public PaginationInnerInterceptor paginationInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDialect(new MySqlDialect());
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        paginationInnerInterceptor.setOverflow(false);
        return paginationInnerInterceptor;
    }

    /**
     * map接收,转驼峰
     * @return ConfigurationCustomizer
     */
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer(){
        return configuration -> configuration.setObjectWrapperFactory(new MapWrapperFactory());
    }

}
