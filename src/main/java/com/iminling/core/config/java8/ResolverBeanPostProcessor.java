package com.iminling.core.config.java8;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iminling.core.config.argument.RequestArgumentResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
public class ResolverBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(beanName.equals("requestMappingHandlerAdapter")){
            RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter)bean;
            final List<HttpMessageConverter<?>> messageConverters = adapter.getMessageConverters();
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
            final ObjectMapper objectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
            objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            objectMapper.registerModule(new MyCustomJava8TimeModule());
            List<HttpMessageConverter<?>> newMessageConverters = new ArrayList<>();
            newMessageConverters.add(mappingJackson2HttpMessageConverter);
            newMessageConverters.addAll(messageConverters);
            adapter.setMessageConverters(newMessageConverters);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // System.out.println("-------------------------------" + beanName);
        if(beanName.equals("requestMappingHandlerAdapter")){
            RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter)bean;
            List<HandlerMethodArgumentResolver> argumentResolvers = adapter.getArgumentResolvers();
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
            final ObjectMapper objectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
            objectMapper.registerModule(new MyCustomJava8TimeModule());
            // 设置日期对象的输出格式
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE));
            // 设置输入时忽略在json字符串中存在 但在java对象实际没有的属性
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            List<HandlerMethodArgumentResolver> newArgumentResolvers = new ArrayList<>();
            newArgumentResolvers.add(new RequestArgumentResolver(objectMapper));
            newArgumentResolvers.addAll(argumentResolvers);
            adapter.setArgumentResolvers(newArgumentResolvers);
        }
        return bean;
    }
}
