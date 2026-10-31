package com.iminling.common.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

class JsonConventionsTest {
    record Payload(long id, LocalDateTime time) {}

    @Test
    void retainsDateAndLongWireFormat() {
        Payload value = new Payload(9007199254740993L, LocalDateTime.of(2026, 10, 31, 12, 30, 10));
        String json = JsonUtil.getInstant().writeValueAsString(value);
        assertEquals(
                "9007199254740993", JsonUtil.getInstant().readTree(json).path("id").asString());
        assertEquals(
                "2026-10-31 12:30:10",
                JsonUtil.getInstant().readTree(json).path("time").asString());
        assertEquals(value, JsonUtil.str2Obj(json, Payload.class));
    }

    @Test
    void serializesJdkValuesWithoutOpeningJdkModules() {
        assertEquals("text", JsonUtil.obj2Str("text"));
        assertNotNull(JsonUtil.obj2Str(Map.of("key", List.of(1, 2))));
        assertEquals("3", JsonUtil.obj2Str(3));
    }
}
