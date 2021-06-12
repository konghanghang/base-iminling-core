package com.iminling.core.config.argument;

import com.iminling.common.json.JsonUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加GlobalArgumentResolver到RequestMappingHandlerAdapter
 * 主要是为了防止包扫描扫描不到com.iminling.core.config.argument.GlobalArgumentResolverConfig
 * 作为一个兜底处理逻辑，使GlobalArgumentResolver生效
 * 该类和GlobalArgumentResolverConfig只会生效一个
 * @author yslao@outlook.com
 * @since 2021/6/12
 */
public class GlobalArgumentBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
            RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
            List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
            argumentResolvers.add(new GlobalArgumentResolver(JsonUtil.getInstant()));
            argumentResolvers.addAll(adapter.getArgumentResolvers());
            adapter.setArgumentResolvers(argumentResolvers);
        }
        return bean;
    }

}
