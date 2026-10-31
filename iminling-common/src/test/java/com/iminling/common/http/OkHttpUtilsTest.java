package com.iminling.common.http;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OkHttpUtilsTest {
    @Test
    void keepsDefaultTimeoutsAndInterceptor() {
        var client = OkHttpUtils.okHttpClientBuilder().build();
        assertEquals(5000, client.connectTimeoutMillis());
        assertEquals(10000, client.readTimeoutMillis());
        assertEquals(10000, client.writeTimeoutMillis());
        assertTrue(
                client.interceptors().stream()
                        .anyMatch(OkHttpLoggingInterceptor.class::isInstance));
    }
}
