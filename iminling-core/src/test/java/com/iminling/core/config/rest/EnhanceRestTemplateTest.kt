package com.iminling.core.config.rest

import com.iminling.common.http.OkHttpUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory

/**
 * @author  yslao@outlook.com
 * @since  2021/11/10
 */
@Disabled
internal class EnhanceRestTemplateTest {

    @Test
    fun createRequest() {
        val okHttp3ClientHttpRequestFactory = OkHttp3ClientHttpRequestFactory(OkHttpUtils.okHttpClientBuilder().build())
        var restTemplate = EnhanceRestTemplate(true)
        restTemplate.requestFactory = okHttp3ClientHttpRequestFactory
        val interceptors = restTemplate.interceptors
        interceptors.add(RestTemplateLoggingInterceptor())
        restTemplate.errorHandler = RestTemplateErrorHandler()
        var forObject = restTemplate.getForObject("http://localhost:8080/index", String::class.java)
        println(forObject)
    }
}