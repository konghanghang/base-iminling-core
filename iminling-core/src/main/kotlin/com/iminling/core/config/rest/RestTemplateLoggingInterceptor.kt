package com.iminling.core.config.rest

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

/**
 * @author yslao@outlook.com
 * @since 2021/11/9
 */
class RestTemplateLoggingInterceptor: ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val response = execution.execute(request, body)
        return ClientHttpResponseWrapper(response)
    }

}