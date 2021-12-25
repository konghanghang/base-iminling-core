package com.iminling.common.http

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.io.File
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 *
 * @author yslao@outlook.com
 * @since 2021/11/7
 */
class OkHttpUtils {

    companion object {

        fun okHttpClientBuilder(): OkHttpClient.Builder {
            return OkHttpClient.Builder().readTimeout(10000, TimeUnit.MILLISECONDS)
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(ConnectionPool(5, 300, TimeUnit.SECONDS))
                .addInterceptor(OkHttpLoggingInterceptor())
        }

        fun okHttpSSLClientBuilder(certificateFilePath: String, password: String, keyStoreType: String): OkHttpClient.Builder {
            var inputStream = File(certificateFilePath).inputStream()
            return okHttpSSLClientBuilder(inputStream, password, keyStoreType)
        }

        /**
         * https://stackoverflow.com/questions/23103174/does-okhttp-support-accepting-self-signed-ssl-certs
         * 首先准备好 证书
         * 获取信任管理器TrustManagerFactory， 秘钥管理器KeyManagerFactory，生成SSLContext
         * 通过SSLContext得到SSLSocketFactory， 通过okHttpClient.builder的sslSocketFactory添加到okhttp
         */
        fun okHttpSSLClientBuilder(certificateFileInputStream: InputStream, password: String, keyStoreType: String): OkHttpClient.Builder {
            var passwordChar = password.toCharArray()

            var keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(certificateFileInputStream, passwordChar)

            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)

            var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, passwordChar)

            var sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagerFactory.keyManagers, null, SecureRandom())
            return okHttpClientBuilder().sslSocketFactory(sslContext.socketFactory,
                trustManagerFactory.trustManagers[0] as X509TrustManager
            )
        }
    }

}