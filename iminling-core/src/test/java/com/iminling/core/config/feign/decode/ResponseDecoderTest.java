package com.iminling.core.config.feign.decode;

import static org.junit.jupiter.api.Assertions.*;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.config.value.ResultModel;

import feign.Request;
import feign.Response;

import org.junit.jupiter.api.Test;

import tools.jackson.core.type.TypeReference;

import java.nio.charset.StandardCharsets;
import java.util.Map;

class ResponseDecoderTest {
    private final ResponseDecoder decoder =
            new ResponseDecoder(
                    (response, type) -> {
                        try (var body = response.body()) {
                            return JsonUtil.getInstant()
                                    .readValue(
                                            body.asInputStream(),
                                            JsonUtil.getInstant().constructType(type));
                        }
                    });

    private Response response(String json) {
        return Response.builder()
                .status(200)
                .reason("OK")
                .headers(Map.of())
                .request(
                        Request.create(
                                Request.HttpMethod.GET,
                                "http://localhost/test",
                                Map.of(),
                                null,
                                StandardCharsets.UTF_8,
                                null))
                .body(json, StandardCharsets.UTF_8)
                .build();
    }

    record Item(String name) {}

    @Test
    void unwrapsTypedDataAndPreservesExplicitEnvelopes() throws Exception {
        assertEquals(
                new Item("test"),
                decoder.decode(
                        response("{\"code\":200,\"data\":{\"name\":\"test\"}}"), Item.class));
        var type = new TypeReference<ResultModel<Item>>() {}.getType();
        var result =
                (ResultModel<?>)
                        decoder.decode(
                                response("{\"code\":200,\"data\":{\"name\":\"test\"}}"), type);
        assertEquals(new Item("test"), result.getData());
        assertEquals(
                "hello",
                decoder.decode(response("{\"code\":200,\"data\":\"hello\"}"), String.class));
    }

    @Test
    void handlesNullDataAndOrdinaryJson() throws Exception {
        assertNull(decoder.decode(response("{\"code\":200,\"data\":null}"), Item.class));
        assertEquals(
                new Item("plain"), decoder.decode(response("{\"name\":\"plain\"}"), Item.class));
    }
}
