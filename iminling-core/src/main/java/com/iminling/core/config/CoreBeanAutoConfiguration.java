package com.iminling.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iminling.common.json.JsonUtil;
import com.iminling.core.ApplicationConstant;
import com.iminling.core.config.argument.DefaultRequestDataReader;
import com.iminling.core.config.argument.GlobalArgumentBeanPostProcessor;
import com.iminling.core.config.argument.GlobalArgumentResolverConfig;
import com.iminling.core.config.exception.GlobalExceptionHandler;
import com.iminling.core.config.mybatis.MybatisConfig;
import com.iminling.core.config.value.GlobalReturnValueHandler;
import com.iminling.core.filter.AuthFilter;
import com.iminling.core.filter.CustomizeGlobalFilter;
import com.iminling.core.filter.Filter;
import com.iminling.core.filter.LoginFilter;
import com.iminling.core.service.ILogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
@Import({MybatisConfig.class})
public class CoreBeanAutoConfiguration implements WebMvcConfigurer {

    /*@Bean
    @ConditionalOnMissingBean(name = "defaultAuthFilter")
    public AuthFilter defaultAuthFilter() {
        return new DefaultAuthFilter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultLoginFilter")
    public LoginFilter defaultLoginFilter() {
        return new DefaultLoginFilter();
    }*/

    @Bean
    @ConditionalOnMissingBean(CustomizeGlobalFilter.class)
    public CustomizeGlobalFilter customizeGlobalFilter() {
        return new CustomizeGlobalFilter();
    }

    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(GlobalReturnValueHandler.class)
    public GlobalReturnValueHandler globalReturnValueHandler() {
        return new GlobalReturnValueHandler();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultRequestDataReader.class)
    public DefaultRequestDataReader defaultRequestDataReader(ObjectMapper objectMapper) {
        return new DefaultRequestDataReader(objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonUtil.getInstant();
    }

    @Bean
    @ConditionalOnMissingBean(GlobalArgumentResolverConfig.class)
    public GlobalArgumentBeanPostProcessor globalArgumentBeanPostProcessor() {
        return new GlobalArgumentBeanPostProcessor();
    }

    /**
     * 生成globalInterceptor
     * @param environment 环境信息
     * @param defaultRequestDataReader 请求数据读取
     * @param loginFilter 登录验证过滤器，有对应bean则size = 0，不会是null
     * @param authFilter 授权验证过滤器，有对应bean则size = 0，不会是null
     * @param logServices 日志记录服务，有对应bean则size = 0，不会是null
     * @return {@link GlobalInterceptor}
     */
    @Bean
    @ConditionalOnMissingBean(GlobalInterceptor.class)
    public GlobalInterceptor globalInterceptor(ConfigurableEnvironment environment,
                                               DefaultRequestDataReader defaultRequestDataReader,
                                               List<LoginFilter> loginFilter, List<AuthFilter> authFilter, List<ILogService> logServices) {
        List<Filter> filters = new ArrayList<>(4);
        String enable = "enable";
        if (environment.getProperty(ApplicationConstant.KEY_FILTER_LOGIN, enable).equals(enable)) {
            filters.addAll(loginFilter);
        }
        if (environment.getProperty(ApplicationConstant.KEY_FILTER_AUTH, enable).equals(enable)) {
            filters.addAll(authFilter);
        }
        if (!environment.getProperty(ApplicationConstant.KEY_FILTER_LOG, enable).equals(enable)) {
            logServices.clear();
        }
        return new GlobalInterceptor(defaultRequestDataReader, filters, logServices, environment);
    }
}
