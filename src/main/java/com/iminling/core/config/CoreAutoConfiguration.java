package com.iminling.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iminling.common.json.JsonUtil;
import com.iminling.core.config.argument.DefaultRequestDataReader;
import com.iminling.core.filter.AuthFilter;
import com.iminling.core.filter.Filter;
import com.iminling.core.filter.LoginFilter;
import com.iminling.core.service.ILogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
@Configuration
public class CoreAutoConfiguration {

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
    @ConditionalOnMissingBean(name = "defaultRequestDataReader")
    public DefaultRequestDataReader defaultRequestDataReader(ObjectMapper objectMapper) {
        return new DefaultRequestDataReader(objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonUtil.getInstant();
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
    @ConditionalOnMissingBean(name = "globalInterceptor")
    public GlobalInterceptor globalInterceptor(ConfigurableEnvironment environment,
                                               DefaultRequestDataReader defaultRequestDataReader,
                                               List<LoginFilter> loginFilter, List<AuthFilter> authFilter, List<ILogService> logServices) {
        List<Filter> filters = new ArrayList<>(4);
        String enable = "enable";
        if (environment.getProperty("filters.login", enable).equals(enable)) {
            filters.addAll(loginFilter);
        }
        if (environment.getProperty("filters.auth", enable).equals(enable)) {
            filters.addAll(authFilter);
        }
        if (!environment.getProperty("filters.log", enable).equals(enable)) {
            logServices.clear();
        }
        boolean enableArgumentLog = environment.getProperty("application.log.argument", Boolean.class, false);
        return new GlobalInterceptor(filters, logServices, defaultRequestDataReader, enableArgumentLog);
    }

    @Configuration
    // @EnableWebMvc
    class CustomMvcConfigure implements WebMvcConfigurer {

        private final GlobalInterceptor globalInterceptor;

        public CustomMvcConfigure(GlobalInterceptor globalInterceptor) {
            this.globalInterceptor = globalInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(globalInterceptor);
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
            registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        }

        /*@Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            // resolvers.add(new RequestArgumentResolver(getObjectMapper()));
        }*/
    }

}
