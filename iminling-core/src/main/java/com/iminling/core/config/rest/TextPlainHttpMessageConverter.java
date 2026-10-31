package com.iminling.core.config.rest;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

public class TextPlainHttpMessageConverter extends JacksonJsonHttpMessageConverter {
    public TextPlainHttpMessageConverter(JsonMapper mapper) {
        super(mapper);
        setSupportedMediaTypes(
                java.util.List.of(new MediaType("text", "plain", StandardCharsets.UTF_8)));
    }
}
