package com.iminling.core.config.argument;

import org.springframework.http.*;
import org.springframework.web.method.HandlerMethod;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.List;
import java.util.Set;

public class DefaultRequestDataReader {
    private final ObjectMapper objectMapper;

    public DefaultRequestDataReader(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public boolean canRead(HttpInputMessage message) {
        MediaType type = message.getHeaders().getContentType();
        if (type != null
                && getSupportedMediaTypes().stream()
                        .noneMatch(supported -> supported.includes(type))) return false;
        return !(message instanceof HttpRequest request)
                || Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)
                        .contains(request.getMethod());
    }

    public JsonNode read(HttpInputMessage message, HandlerMethod handler) throws IOException {
        PushbackInputStream input = new PushbackInputStream(message.getBody());
        int first = input.read();
        if (first == -1) return null;
        input.unread(first);
        return objectMapper.readTree(input);
    }

    public List<MediaType> getSupportedMediaTypes() {
        return List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN);
    }
}
