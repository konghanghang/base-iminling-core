package com.iminling.common.http;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class OkHttpUtils {
    private OkHttpUtils() {}

    public static OkHttpClient.Builder okHttpClientBuilder() {
        return new OkHttpClient.Builder()
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(new ConnectionPool(5, 300, TimeUnit.SECONDS))
                .addInterceptor(new OkHttpLoggingInterceptor());
    }

    public static OkHttpClient.Builder okHttpSSLClientBuilder(
            String path, String password, String type)
            throws GeneralSecurityException, IOException {
        try (InputStream input = Files.newInputStream(Path.of(path))) {
            return okHttpSSLClientBuilder(input, password, type);
        }
    }

    public static OkHttpClient.Builder okHttpSSLClientBuilder(
            InputStream input, String password, String type)
            throws GeneralSecurityException, IOException {
        KeyStore store = KeyStore.getInstance(type);
        store.load(input, password.toCharArray());
        TrustManagerFactory trust =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(store);
        KeyManagerFactory keys =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keys.init(store, password.toCharArray());
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keys.getKeyManagers(), trust.getTrustManagers(), new SecureRandom());
        return okHttpClientBuilder()
                .sslSocketFactory(
                        context.getSocketFactory(), (X509TrustManager) trust.getTrustManagers()[0]);
    }
}
