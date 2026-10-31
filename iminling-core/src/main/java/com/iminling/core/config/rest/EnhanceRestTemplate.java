package com.iminling.core.config.rest;

import com.iminling.common.http.OkHttpUtils;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.*;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EnhanceRestTemplate extends RestTemplate {
    private final boolean enableTimeOut;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final ConcurrentHashMap<String, ClientHttpRequestFactory> factories =
            new ConcurrentHashMap<>();

    public EnhanceRestTemplate(boolean enableTimeOut) {
        this.enableTimeOut = enableTimeOut;
        matcher.setCaseSensitive(false);
    }

    @Override
    protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
        if (!enableTimeOut) return super.createRequest(url, method);
        UrlProperties config =
                initUrl().stream()
                        .filter(
                                item ->
                                        (item.getMethod() == null
                                                        || item.getMethod()
                                                                .equalsIgnoreCase(method.name()))
                                                && matcher.match(item.getUrl(), url.getPath()))
                        .findFirst()
                        .orElse(null);
        if (config == null) return super.createRequest(url, method);
        return factories
                .computeIfAbsent(
                        config.getUrl(),
                        key ->
                                new OkHttpClientHttpRequestFactory(
                                        OkHttpUtils.okHttpClientBuilder()
                                                .connectTimeout(
                                                        config.getConnectTimeout(),
                                                        TimeUnit.MILLISECONDS)
                                                .readTimeout(
                                                        config.getReadTimeout(),
                                                        TimeUnit.MILLISECONDS)
                                                .writeTimeout(
                                                        config.getWriteTimeout(),
                                                        TimeUnit.MILLISECONDS)
                                                .build()))
                .createRequest(url, method);
    }

    private List<UrlProperties> initUrl() {
        UrlProperties config = new UrlProperties();
        config.setUrl("/index");
        config.setMethod("GET");
        config.setConnectTimeout(1000);
        config.setReadTimeout(15000);
        config.setWriteTimeout(1000);
        return List.of(config);
    }
}
