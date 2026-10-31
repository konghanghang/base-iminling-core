package com.iminling.core.config.feign.client;

import feign.Client;
import feign.Request;
import feign.Response;

import okhttp3.OkHttpClient;

import java.io.IOException;

/** Uses Feign's supported OkHttp adapter for request bodies, timeouts and streaming responses. */
public class CustomizeFeignClient implements Client {
    private final feign.okhttp.OkHttpClient delegate;

    public CustomizeFeignClient(OkHttpClient client) {
        delegate = new feign.okhttp.OkHttpClient(client);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        return delegate.execute(request, options);
    }
}
