package com.iminling.common.http

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author yslao@outlook.com
 * @since 2021/11/7
 */
class OkHttpLoggingInterceptor : Interceptor {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    // header全部转成小写进行比较
    var ignoreHeaders = setOf(
        "user-agent", "cookie", "accept", "sec-fetch-dest", "accept-language",
        "cache-control", "sec-fetch-mode", "connection", "accept-encoding", "upgrade-insecure-requests",
        "sec-fetch-site", "sec-fetch-user"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        var start = System.currentTimeMillis()
        var request = chain.request()
        var url = request.url.toString()
        var method = request.method
        var body = request.body?.let {
            var copy = request.newBuilder().build()
            var buffer = Buffer()
            copy.body?.writeTo(buffer)
            buffer.readUtf8()
        }
        var headers = request.headers.toMultimap()
        headers = headers.filter { !ignoreHeaders.contains(it.key.lowercase()) }
        log.info("OKHTTP_0, url:{}, method:{}, body:{}, headers:{}", url, method, body?.replace("\n", ""), headers)
        var exception: Exception? = null
        var response: Response? = null
        try {
            response = chain.proceed(request)
            return response
        } catch (e: Exception) {
            exception = e
            throw e
        } finally {
            var end = System.currentTimeMillis()
            var source = response?.body?.source()
            source?.request(Long.MAX_VALUE)
            var buffer = source?.buffer
            var res = buffer?.clone()?.readUtf8()
            var headers = response?.headers?.toMultimap()
            headers = headers?.filter { !ignoreHeaders.contains(it.key.lowercase()) }
            log.info(
                "OKHTTP_1,  url:{}, method:{}, status:{}, times:{}, response:{}, error:{}, headers:{}",
                url,
                method,
                response?.code,
                end - start,
                res?.replace("\n", ""),
                exception?.message,
                headers
            )
        }
    }
}