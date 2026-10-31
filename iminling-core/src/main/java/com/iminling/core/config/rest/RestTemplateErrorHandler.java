package com.iminling.core.config.rest;

import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

public class RestTemplateErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(URI uri, HttpMethod method, ClientHttpResponse response)
            throws IOException {
        throw new RuntimeException(
                new String(getResponseBody(response), getCharset(response)).trim());
    }

    @Override
    protected Charset getCharset(ClientHttpResponse response) {
        MediaType type = response.getHeaders().getContentType();
        return type != null && type.getCharset() != null
                ? type.getCharset()
                : Charset.defaultCharset();
    }
}
