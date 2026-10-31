package com.iminling.core.config.feign.decode;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.config.value.ResultModel;

import feign.Response;
import feign.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ResponseDecoder implements Decoder {
    private final Decoder delegate;

    public ResponseDecoder(Decoder decoder) {
        delegate = decoder;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        Type raw =
                type instanceof ParameterizedType parameterized ? parameterized.getRawType() : type;
        if (raw instanceof Class<?> clazz && ResultModel.class.isAssignableFrom(clazz))
            return delegate.decode(response, type);
        if (response.body() == null) return null;
        byte[] bytes;
        try (var body = response.body()) {
            bytes = body.asInputStream().readAllBytes();
        }
        if (bytes.length == 0) return null;
        var node = JsonUtil.getInstant().readTree(bytes);
        if (node == null || node.isNull()) return null;
        // Ordinary JSON responses are still decoded by the standard decoder.
        if (!node.isObject() || !node.has("code") || !node.has("data"))
            return delegate.decode(response.toBuilder().body(bytes).build(), type);
        var data = node.path("data");
        if (data.isNull() || data.isMissingNode()) return null;
        if (raw == String.class && data.isString()) return data.asString();
        return delegate.decode(
                response.toBuilder().body(JsonUtil.getInstant().writeValueAsBytes(data)).build(),
                type);
    }
}
