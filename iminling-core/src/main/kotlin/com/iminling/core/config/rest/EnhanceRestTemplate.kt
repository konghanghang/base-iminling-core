package com.iminling.core.config.rest

import com.iminling.common.http.OkHttpUtils
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.util.AntPathMatcher
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * @author yslao@outlook.com
 * @since 2021/11/10
 */
class EnhanceRestTemplate(private val enableTimeOut: Boolean) : RestTemplate() {

    private val pathMatcher = AntPathMatcher().also {
        it.setCaseSensitive(false)
        it.setTrimTokens(false)
        it.setPathSeparator("/")
    }
    private val factoryMap = ConcurrentHashMap<String, OkHttp3ClientHttpRequestFactory>()

    override fun createRequest(url: URI, method: HttpMethod): ClientHttpRequest {
        if (!enableTimeOut) {
            return super.createRequest(url, method)
        }
        var urlProperties = findConfig(url, method)
        var requestFactory= urlProperties?.let {
            factoryMap.computeIfAbsent(it.url as String) {
                OkHttp3ClientHttpRequestFactory(OkHttpUtils.okHttpClientBuilder().apply {
                    connectTimeout(urlProperties.connectTimeout?.toLong() as Long, TimeUnit.MILLISECONDS)
                    readTimeout(urlProperties.readTimeout?.toLong() as Long, TimeUnit.MILLISECONDS)
                    writeTimeout(urlProperties.writeTimeout?.toLong() as Long, TimeUnit.MILLISECONDS)
                }.build())
            }
        }
        return requestFactory?.createRequest(url, method) ?: super.createRequest(url, method)
    }

    private fun findConfig(url: URI, method: HttpMethod): UrlProperties? {
        val host: String = url.host + ":" + url.port
        println("host:$host")
        var urls = initUrl()
        var find = urls.find {
            (it.method == null || it.method.equals(method.name, true)) && pathMatcher.match(
                it.url as String,
                url.path
            )
        }
        return find
    }

    /**
     * 后续根据配置文件进行初始化
     */
    private fun initUrl(): List<UrlProperties> {
        val properties = UrlProperties()
        properties.url = "/index"
        properties.method = "GET"
        properties.connectTimeout = 1000
        properties.readTimeout = 15000
        properties.writeTimeout = 1000
        return listOf(properties)
    }

}