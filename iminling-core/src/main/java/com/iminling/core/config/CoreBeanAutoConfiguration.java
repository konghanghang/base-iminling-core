package com.iminling.core.config;

import com.iminling.common.http.OkHttpUtils;
import com.iminling.common.json.JsonUtil;
import com.iminling.core.ApplicationConstant;
import com.iminling.core.config.argument.*;
import com.iminling.core.config.exception.*;
import com.iminling.core.config.filter.*;
import com.iminling.core.config.rest.*;
import com.iminling.core.config.value.GlobalReturnValueHandler;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@AutoConfiguration(
        beforeName = "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(GlobalSpringErrorHandlerConfig.class)
public class CoreBeanAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public com.iminling.core.validation.ValidationInterceptor validationInterceptor() {
        return new com.iminling.core.validation.ValidationInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomizeGlobalFilter customizeGlobalFilter() {
        return new CustomizeGlobalFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalReturnValueHandler globalReturnValueHandler() {
        return new GlobalReturnValueHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonMapper objectMapper() {
        return JsonUtil.getInstant();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultRequestDataReader defaultRequestDataReader(JsonMapper mapper) {
        return new DefaultRequestDataReader(mapper);
    }

    @Bean
    @ConditionalOnMissingBean({
        GlobalArgumentResolverConfig.class,
        GlobalArgumentBeanPostProcessor.class
    })
    public static GlobalArgumentBeanPostProcessor globalArgumentBeanPostProcessor(
            org.springframework.beans.factory.ObjectProvider<JsonMapper> mappers) {
        return new GlobalArgumentBeanPostProcessor(mappers::getObject);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalInterceptor globalInterceptor(
            ConfigurableEnvironment environment,
            DefaultRequestDataReader reader,
            List<LoginFilter> loginFilters,
            List<AuthFilter> authFilters) {
        List<com.iminling.core.config.filter.Filter> filters = new ArrayList<>();
        if ("enable"
                .equals(environment.getProperty(ApplicationConstant.KEY_FILTER_LOGIN, "enable")))
            filters.addAll(loginFilters);
        if ("enable".equals(environment.getProperty(ApplicationConstant.KEY_FILTER_AUTH, "enable")))
            filters.addAll(authFilters);
        return new GlobalInterceptor(reader, filters, environment);
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(ConfigurableEnvironment environment, JsonMapper mapper) {
        var template =
                new EnhanceRestTemplate(
                        environment.getProperty(
                                ApplicationConstant.KEY_REST_TIMEOUT, Boolean.class, false));
        template.setRequestFactory(
                new OkHttpClientHttpRequestFactory(OkHttpUtils.okHttpClientBuilder().build()));
        template.setErrorHandler(new RestTemplateErrorHandler());
        var converters = template.getMessageConverters();
        converters.removeIf(converter -> converter instanceof JacksonJsonHttpMessageConverter);
        converters.add(new JacksonJsonHttpMessageConverter(mapper));
        for (var converter : converters)
            if (converter instanceof StringHttpMessageConverter text)
                text.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(new TextPlainHttpMessageConverter(mapper));
        return template;
    }
}
