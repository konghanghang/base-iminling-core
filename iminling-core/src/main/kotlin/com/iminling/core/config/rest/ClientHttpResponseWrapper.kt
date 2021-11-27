package com.iminling.core.config.rest

import com.iminling.common.json.JsonUtil
import com.iminling.core.config.value.ResultModel
import org.springframework.http.HttpHeaders
import org.springframework.http.client.AbstractClientHttpResponse
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * @author yslao@outlook.com
 * @since 2021/11/9
 */
class ClientHttpResponseWrapper(private val response: ClientHttpResponse): AbstractClientHttpResponse() {

    var inputStream: InputStream? = null
    var inner: Boolean = false

    override fun getHeaders(): HttpHeaders {
        return response.headers
    }

    override fun getBody(): InputStream {
        if (inputStream == null) {
            // 是否是内部调用
            if (!inner) {
                inputStream = response.body
                return inputStream as InputStream
            }
            var body = response.body
            var byteArrayOutputStream = ByteArrayOutputStream()
            StreamUtils.copy(body, byteArrayOutputStream)
            var objectMapper = JsonUtil.getInstant()
            var readValue = objectMapper.readValue(byteArrayOutputStream.toByteArray(), ResultModel::class.java)
            inputStream = ByteArrayInputStream(objectMapper.writeValueAsBytes(readValue.data))
        }
        return inputStream as InputStream
    }

    override fun close() {
        inputStream?.let {
            StreamUtils.drain(it)
            it.close()
        }
        response.close()
    }

    override fun getRawStatusCode(): Int {
        return response.rawStatusCode
    }

    override fun getStatusText(): String {
        return response.statusText
    }
}