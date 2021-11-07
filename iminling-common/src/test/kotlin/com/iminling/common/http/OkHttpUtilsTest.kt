package com.iminling.common.http

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author yslao@outlook.com
 * @since 2021/11/7
 */
internal class OkHttpUtilsTest {

    private lateinit var httpClient: OkHttpClient
    private var response: Response? = null

    @BeforeEach
    fun beforeEach() {
        httpClient = OkHttpUtils.okHttpClientBuilder().build()
    }

    @AfterEach
    fun afterEach() {
        response?.body?.close()
    }

    @Test
    fun testHttp() {
        var build = Request.Builder()
            .url("https://test.com/article/chosen")
            .header("aaa", "aaa")
            .header("User-Agent", "okhttp")
            .get()
            .build()
        var res = httpClient.newCall(build).execute()
        var resStr = res.body?.string()
        println(resStr)
    }

}