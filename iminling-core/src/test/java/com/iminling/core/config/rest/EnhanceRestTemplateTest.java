package com.iminling.core.config.rest;

import static org.junit.jupiter.api.Assertions.*;

import com.iminling.common.http.OkHttpUtils;
import com.iminling.common.json.JsonUtil;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class EnhanceRestTemplateTest {
    @Test
    void usesSpring7RequestFactoryAndRetainsPlainTextJsonAndErrorBody() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(
                "/index",
                exchange -> {
                    byte[] body = "{\"name\":\"你好\"}".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain;charset=UTF-8");
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                    exchange.close();
                });
        server.createContext(
                "/error",
                exchange -> {
                    byte[] body = "错误详情".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain;charset=UTF-8");
                    exchange.sendResponseHeaders(500, body.length);
                    exchange.getResponseBody().write(body);
                    exchange.close();
                });
        server.start();
        try {
            for (boolean perUrlTimeout : new boolean[] {false, true}) {
                var client = new EnhanceRestTemplate(perUrlTimeout);
                client.setRequestFactory(
                        new OkHttpClientHttpRequestFactory(
                                OkHttpUtils.okHttpClientBuilder().build()));
                client.setErrorHandler(new RestTemplateErrorHandler());
                client.getMessageConverters()
                        .add(new TextPlainHttpMessageConverter(JsonUtil.getInstant()));
                String url = "http://127.0.0.1:" + server.getAddress().getPort();
                assertEquals("你好", client.getForObject(url + "/index", Map.class).get("name"));
                assertEquals(
                        "你好",
                        client.exchange(
                                        url + "/index",
                                        HttpMethod.PATCH,
                                        new HttpEntity<>("{}"),
                                        Map.class)
                                .getBody()
                                .get("name"));
                RuntimeException error =
                        assertThrows(
                                RuntimeException.class,
                                () -> client.getForObject(url + "/error", String.class));
                assertEquals("错误详情", error.getMessage());
            }
        } finally {
            server.stop(0);
        }
    }
}
