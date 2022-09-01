package com.iminling.core.config.feign

import com.iminling.common.http.OkHttpUtils.Companion.okHttpClientBuilder
import com.iminling.core.config.feign.decode.ResponseDecoder
import feign.Client
import feign.Feign
import feign.codec.Decoder
import feign.okhttp.OkHttpClient
import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * feign相关配置
 * @author yslao@outlook.com
 * @since 2022/5/20
 */
@ConditionalOnClass(Feign::class)
@ConditionalOnProperty(prefix = "app.customize", name = ["feign"], havingValue = "true", matchIfMissing = true)
class CustomizeFeignAutoConfiguration(
    private val messageConverters: ObjectFactory<HttpMessageConverters?>
    ) {

    /**
     * 使用自定义的feignClient发起http请求
     */
    @Bean
    @ConditionalOnMissingBean
    fun feignClient(
        loadBalancerClient: LoadBalancerClient,
        loadBalancerClientFactory: LoadBalancerClientFactory
    ): Client {
        val okHttpClient = okHttpClientBuilder()
            .retryOnConnectionFailure(false).build()
        return FeignBlockingLoadBalancerClient(
            OkHttpClient(okHttpClient),
            loadBalancerClient,
            loadBalancerClientFactory
        )
    }

    /**
     * 自定义feign解码，主要是处理 ResultModel 类型
     */
    @Bean
    @ConditionalOnMissingBean
    fun feignDecoder(): Decoder {
        return ResponseEntityDecoder(ResponseDecoder(SpringDecoder(messageConverters)))
    }

    /**
     * 处理：@FeignClient中加在类上的@RequestMapping也被SpringMVC加载的问题解决
     *
     * <p>
     * <pre> {@code
     * @FeignClient(name = "provider", contextId = "productInfo")
     * @RequestMapping("/product")
     * public interface ProductInfoClient {
     *  @PostMapping("/add")
     *  Integer add(@RequestBody ProductInfo info);
     * }}</pre>
     * </p>
     * @return
     */
    @Bean
    fun feignWebRegistrations(): WebMvcRegistrations {
        return object : WebMvcRegistrations {
            override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping {
                return FeignRequestMappingHandlerMapping()
            }
        }
    }

    /**
     * 重写判断是否是Handler方法
     * 要求类本身没有FeignClient注解
     */
    private class FeignRequestMappingHandlerMapping : RequestMappingHandlerMapping() {
        override fun isHandler(beanType: Class<*>): Boolean {
            return super.isHandler(beanType) &&
                    !AnnotatedElementUtils.isAnnotated(beanType, FeignClient::class.java)
        }
    }

}