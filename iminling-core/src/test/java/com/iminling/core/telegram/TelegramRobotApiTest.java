package com.iminling.core.telegram;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TelegramRobotApiTest {
    @Disabled("Manual integration test: requires Telegram credentials and sends a real message")
    @Test
    void sendMessageTest() throws Exception {
        TelegramRobotApi.sendMessage(
                System.getenv("TELEGRAM_BOT_TOKEN"), System.getenv("TELEGRAM_CHAT_ID"), "测试消息");
    }
}
