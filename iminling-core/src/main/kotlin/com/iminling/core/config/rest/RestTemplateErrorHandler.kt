package com.iminling.core.config.rest

import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import java.nio.charset.Charset

/**
 * @author yslao@outlook.com
 * @since 2021/11/9
 */
class RestTemplateErrorHandler: DefaultResponseErrorHandler() {

    override fun handleError(response: ClientHttpResponse) {
        val charset = getCharset(response)
        val responseBody = getResponseBody(response)
        var responseText: String? = null
        if (charset != null && responseBody.isNotEmpty()) {
            // 可以避免某些特殊空格字符的被切除掉 it <= ' '
            responseText = String(responseBody, charset).trim { it <= ' ' }
        }
        throw RuntimeException(responseText)
    }

    override fun getCharset(response: ClientHttpResponse): Charset? {
        val headers = response.headers
        val contentType = headers.contentType
        return if (contentType != null && contentType.charset != null) contentType.charset else Charset.defaultCharset()
    }
}