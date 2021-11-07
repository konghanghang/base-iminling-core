package com.iminling.common.http

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 *
 * @author yslao@outlook.com
 * @since 2021/11/7
 */
class OkHttpUtils {

    companion object {
        fun okHttpClientBuilder(): OkHttpClient.Builder {
            return OkHttpClient.Builder().readTimeout(10000, TimeUnit.MILLISECONDS)
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(ConnectionPool(10, 600, TimeUnit.SECONDS))
                .addInterceptor(OkHttpLoggingInterceptor());
        }
    }

}