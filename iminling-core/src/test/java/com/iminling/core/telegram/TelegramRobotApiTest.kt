package com.iminling.core.telegram

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * @author  yslao@outlook.com
 * @since  2022/10/10
 */
internal class TelegramRobotApiTest {

    @Disabled
    @Test
    fun sendMessageTest() {
        var token = "572545465546:AAR0dgrhhiW2fow9T_sLw1Hg"
        var chatId = "949124212449"
        var content = "我是一条测试信息"
        TelegramRobotApi.sendMessage(token, chatId, content)
    }

}