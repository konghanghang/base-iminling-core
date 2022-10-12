package com.iminling.core.telegram

import com.iminling.common.http.OkHttpUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @author  yslao@outlook.com
 * @since  2022/10/9
 */
class TelegramRobotApi {

    companion object {
        private var okHttpClient = OkHttpUtils.okHttpClientBuilder().build()

        private var host:String = "https://api.telegram.org/bot{TOKEN}"

        fun sendMessage(token:String, chatId:String, content:String) {
            var url = "${host.replace("{TOKEN}", token)}/sendMessage"
            var body = """
                {
                    "chat_id": "$chatId",
                    "text": "$content"
                }
            """.trimIndent()
            var request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            okHttpClient.newCall(request).execute()
        }
    }

}