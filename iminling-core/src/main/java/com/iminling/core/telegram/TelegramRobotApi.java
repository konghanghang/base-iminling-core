package com.iminling.core.telegram;

import com.iminling.common.http.OkHttpUtils;
import com.iminling.common.json.JsonUtil;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public final class TelegramRobotApi {
    private static final OkHttpClient CLIENT = OkHttpUtils.okHttpClientBuilder().build();

    private TelegramRobotApi() {}

    public static void sendMessage(String token, String chatId, String content) throws IOException {
        String json =
                JsonUtil.getInstant()
                        .writeValueAsString(Map.of("chat_id", chatId, "text", content));
        Request request =
                new Request.Builder()
                        .url("https://api.telegram.org/bot" + token + "/sendMessage")
                        .post(RequestBody.create(json, MediaType.get("application/json")))
                        .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.body() != null) response.body().close();
        }
    }
}
