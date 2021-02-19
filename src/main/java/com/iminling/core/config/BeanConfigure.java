package com.iminling.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iminling.core.config.argument.DefaultRequestDataReader;
import com.iminling.core.filter.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
@Configuration
public class BeanConfigure {

    @Bean
    @ConditionalOnMissingBean(name = "authFilter")
    public AuthFilter authFilter() {
        return new DefaultAuthFilter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "loginFilter")
    public LoginFilter loginFilter() {
        return new DefaultLoginFilter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultRequestDataReader")
    public DefaultRequestDataReader defaultRequestDataReader() {
        return new DefaultRequestDataReader();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 设置日期对象的输出格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINESE));
        // 设置输入时忽略在json字符串中存在 但在java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(name = "globalInterceptor")
    public GlobalInterceptor getGlobalInterceptor() {
        List<Filter> filters = new ArrayList<>(4);
        filters.add(loginFilter());
        filters.add(authFilter());
        return new GlobalInterceptor(filters, defaultRequestDataReader());
    }

}
