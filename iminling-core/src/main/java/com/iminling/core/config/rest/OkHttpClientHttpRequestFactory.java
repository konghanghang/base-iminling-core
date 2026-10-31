package com.iminling.core.config.rest;

import okhttp3.*;

import org.springframework.http.*;
import org.springframework.http.client.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/** Spring 7 replacement for the removed Spring OkHttp request factory. */
public class OkHttpClientHttpRequestFactory implements ClientHttpRequestFactory {
    private final OkHttpClient client;

    public OkHttpClientHttpRequestFactory(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod method) {
        return new AbstractBufferingClientHttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return method;
            }

            @Override
            public URI getURI() {
                return uri;
            }

            @Override
            protected ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bytes)
                    throws IOException {
                Request.Builder builder = new Request.Builder().url(uri.toURL());
                headers.forEach(
                        (name, values) -> values.forEach(value -> builder.addHeader(name, value)));
                org.springframework.http.MediaType contentType = headers.getContentType();
                boolean canHaveBody =
                        !HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method);
                RequestBody body =
                        canHaveBody
                                ? RequestBody.create(
                                        bytes,
                                        contentType == null
                                                ? null
                                                : okhttp3.MediaType.parse(contentType.toString()))
                                : null;
                Response response =
                        client.newCall(builder.method(method.name(), body).build()).execute();
                return new ClientHttpResponse() {
                    @Override
                    public HttpStatusCode getStatusCode() {
                        return HttpStatusCode.valueOf(response.code());
                    }

                    @Override
                    public String getStatusText() {
                        return response.message();
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        HttpHeaders result = new HttpHeaders();
                        response.headers().toMultimap().forEach(result::put);
                        return result;
                    }

                    @Override
                    public InputStream getBody() {
                        return response.body() == null
                                ? InputStream.nullInputStream()
                                : response.body().byteStream();
                    }

                    @Override
                    public void close() {
                        response.close();
                    }
                };
            }
        };
    }
}
