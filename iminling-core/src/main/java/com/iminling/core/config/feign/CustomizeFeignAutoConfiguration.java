package com.iminling.core.config.feign;

import com.iminling.common.http.OkHttpUtils;
import com.iminling.core.config.feign.client.CustomizeFeignClient;
import com.iminling.core.config.feign.decode.ResponseDecoder;

import feign.Client;
import feign.Feign;
import feign.codec.Decoder;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.loadbalancer.*;
import org.springframework.cloud.openfeign.support.*;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration(
        beforeName = {
            "org.springframework.cloud.openfeign.FeignAutoConfiguration",
            "org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration"
        })
@ConditionalOnClass({Feign.class, LoadBalancerClientFactory.class})
@ConditionalOnProperty(
        prefix = "app.customize",
        name = "feign",
        havingValue = "true",
        matchIfMissing = true)
public class CustomizeFeignAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(Client.class)
    public Client feignClient(
            ObjectProvider<LoadBalancerClient> clients,
            ObjectProvider<LoadBalancerClientFactory> factories,
            ObjectProvider<LoadBalancerFeignRequestTransformer> transformers) {
        Client delegate =
                new CustomizeFeignClient(
                        OkHttpUtils.okHttpClientBuilder().retryOnConnectionFailure(false).build());
        LoadBalancerClient client = clients.getIfAvailable();
        LoadBalancerClientFactory factory = factories.getIfAvailable();
        return client == null || factory == null
                ? delegate
                : new FeignBlockingLoadBalancerClient(
                        delegate, client, factory, transformers.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean(Decoder.class)
    public Decoder feignDecoder(ObjectProvider<FeignHttpMessageConverters> converters) {
        return new ResponseEntityDecoder(new ResponseDecoder(new SpringDecoder(converters)));
    }

    @Bean
    public WebMvcRegistrations feignWebRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RequestMappingHandlerMapping() {
                    @Override
                    protected boolean isHandler(Class<?> type) {
                        return super.isHandler(type)
                                && !AnnotatedElementUtils.hasAnnotation(type, FeignClient.class);
                    }
                };
            }
        };
    }
}
