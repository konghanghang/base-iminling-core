package com.iminling.core.config.feign.client

import feign.Client
import feign.Request
import feign.Response
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * 自定义feign客户端
 * @author yslao@outlook.com
 * @since 2022/5/21
 */
class CustomizeFeignClient(
    private val delegate: OkHttpClient
    ): Client {
    override fun execute(request: Request, options: Request.Options): Response {
        var requestClient = if (delegate.connectTimeoutMillis != options.connectTimeoutMillis()
            || delegate.readTimeoutMillis != options.readTimeoutMillis()
        ) {
            delegate.newBuilder()
                .connectTimeout(options.connectTimeoutMillis().toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(options.readTimeoutMillis().toLong(), TimeUnit.MILLISECONDS)
                .build()
        } else {
            delegate
        }
        var okHttpRequest = toOkHttpRequest(request)
        val response = requestClient.newCall(okHttpRequest).execute()
        return toFeignResponse(response, request)
    }

    companion object {
        private fun toOkHttpRequest(request: Request): okhttp3.Request {
            val requestBuilder = okhttp3.Request.Builder()
            requestBuilder.url(request.url())
            var mediaType: MediaType? = null
            var hasAcceptHeader = false
            for (field in request.headers().keys) {
                if ("Accept".equals(field, ignoreCase = true)) {
                    hasAcceptHeader = true
                }
                for (value in request.headers()[field]!!) {
                    if ("Content-Type".equals(field, ignoreCase = true)) {
                        mediaType = value.toMediaTypeOrNull()
                        if (request.charset() != null && mediaType != null) {
                            mediaType.charset(request.charset())
                        }
                    } else {
                        requestBuilder.addHeader(field, value)
                    }
                }
            }
            if (!hasAcceptHeader) {
                requestBuilder.addHeader("Accept", "*/*")
            }
            var inputBody = request.requestTemplate().body()
            val isMethodWithBody =
                Request.HttpMethod.POST == request.httpMethod() || Request.HttpMethod.PUT == request.httpMethod()
            if (isMethodWithBody && inputBody == null) {
                // write an empty BODY to conform with okhttp 2.4.0+
                // http://johnfeng.github.io/blog/2015/06/30/okhttp-updates-post-wouldnt-be-allowed-to-have-null-body/
                inputBody = ByteArray(0)
            }

            val body = inputBody?.toRequestBody(mediaType)
            requestBuilder.method(request.httpMethod().name, body)
            return requestBuilder.build()
        }

        private fun toFeignResponse(response: okhttp3.Response, request: feign.Request): feign.Response {
            return Response.builder()
                .status(response.code)
                .reason(response.message)
                .headers(toMap(response.headers))
                .body(toBody(response.body))
                .request(request)
                .build()
        }

        private fun toMap(headers: Headers): Map<String, Collection<String>> {
            return headers.toMultimap()
        }

        @Throws(IOException::class)
        private fun toBody(responseBody: ResponseBody?): Response.Body? {
            if (responseBody == null || responseBody.contentLength() == 0L) {
                responseBody?.close()
                return null
            }
            val length =
                if (responseBody.contentLength() >= 0 && responseBody.contentLength() <= Int.MAX_VALUE) responseBody.contentLength()
                    .toInt() else null
            return object : Response.Body {
                @Throws(IOException::class)
                override fun close() {
                    responseBody.close()
                }

                override fun length(): Int {
                    return length!!
                }

                override fun isRepeatable(): Boolean {
                    return false
                }

                @Throws(IOException::class)
                override fun asInputStream(): InputStream {
                    return responseBody.byteStream()
                }

                @Throws(IOException::class)
                override fun asReader(charset: Charset): Reader {
                    return responseBody.charStream()
                }
            }
        }
    }
}