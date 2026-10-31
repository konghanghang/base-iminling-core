package com.iminling.common.http;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import okio.Buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class OkHttpLoggingInterceptor implements Interceptor {
    private static final Logger LOG = LoggerFactory.getLogger(OkHttpLoggingInterceptor.class);
    private Set<String> ignoreHeaders =
            Set.of(
                    "user-agent",
                    "cookie",
                    "accept",
                    "sec-fetch-dest",
                    "accept-language",
                    "cache-control",
                    "sec-fetch-mode",
                    "connection",
                    "accept-encoding",
                    "upgrade-insecure-requests",
                    "sec-fetch-site",
                    "sec-fetch-user");

    public Set<String> getIgnoreHeaders() {
        return ignoreHeaders;
    }

    public void setIgnoreHeaders(Set<String> headers) {
        ignoreHeaders = headers;
    }

    private Map<String, List<String>> filtered(Map<String, List<String>> headers) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        headers.forEach(
                (key, value) -> {
                    if (!ignoreHeaders.contains(key.toLowerCase(Locale.ROOT)))
                        result.put(key, value);
                });
        return result;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        long start = System.currentTimeMillis();
        Request request = chain.request();
        String body = null;
        if (request.body() != null && !request.body().isDuplex() && !request.body().isOneShot()) {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            body = buffer.readUtf8().replace("\n", "");
        }
        LOG.info(
                "OKHTTP_0, url:{}, method:{}, body:{}, headers:{}",
                request.url(),
                request.method(),
                body,
                filtered(request.headers().toMultimap()));
        Response response = null;
        IOException error = null;
        try {
            response = chain.proceed(request);
            return response;
        } catch (IOException e) {
            error = e;
            throw e;
        } finally {
            String result = null;
            if (response != null && response.body() != null) {
                ResponseBody responseBody = response.body();
                responseBody.source().request(Long.MAX_VALUE);
                result = responseBody.source().getBuffer().clone().readUtf8().replace("\n", "");
            }
            LOG.info(
                    "OKHTTP_1, url:{}, method:{}, status:{}, times:{}, response:{}, error:{},"
                            + " headers:{}",
                    request.url(),
                    request.method(),
                    response == null ? null : response.code(),
                    System.currentTimeMillis() - start,
                    result,
                    error == null ? null : error.getMessage(),
                    response == null ? null : filtered(response.headers().toMultimap()));
        }
    }
}
