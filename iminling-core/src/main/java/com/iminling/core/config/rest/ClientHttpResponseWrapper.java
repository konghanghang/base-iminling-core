package com.iminling.core.config.rest;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.config.value.ResultModel;

import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;

import java.io.*;

public class ClientHttpResponseWrapper implements ClientHttpResponse {
    private final ClientHttpResponse response;
    private InputStream inputStream;
    private boolean inner;

    public ClientHttpResponseWrapper(ClientHttpResponse response) {
        this.response = response;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public boolean getInner() {
        return inner;
    }

    public void setInner(boolean inner) {
        this.inner = inner;
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public InputStream getBody() throws IOException {
        if (inputStream == null) {
            if (!inner) inputStream = response.getBody();
            else {
                ResultModel<?> result =
                        JsonUtil.getInstant().readValue(response.getBody(), ResultModel.class);
                inputStream =
                        new ByteArrayInputStream(
                                JsonUtil.getInstant().writeValueAsBytes(result.getData()));
            }
        }
        return inputStream;
    }

    @Override
    public void close() {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException ignored) {
        } finally {
            response.close();
        }
    }
}
