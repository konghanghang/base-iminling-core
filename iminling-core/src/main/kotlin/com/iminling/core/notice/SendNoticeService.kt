package com.iminling.core.notice

/**
 * @author  yslao@outlook.com
 * @since  2022/10/11
 */
interface SendNoticeService {

    fun sendNotice(chatId: String, content: String)

}