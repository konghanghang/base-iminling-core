package com.iminling.core.config.rest;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;

import java.io.IOException;

public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        return new ClientHttpResponseWrapper(execution.execute(request, body));
    }
}
