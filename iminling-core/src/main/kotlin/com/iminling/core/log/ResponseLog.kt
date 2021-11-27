package com.iminling.core.log

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class ResponseLog {

    /**
     * 请求URI
     */
    var uri: String? = null

    /**
     * 请求http消息类型
     */
    var httpMethod: String? = null

    /**
     * 自定义消息体状态
     */
    var respStatus: Int? = null

    var message: String? = null

    /**
     * 返回的http状态码
     */
    var status = 0

    /**
     * 响应时间(ms)
     */
    var time: Long = 0

    /**
     * 返回消息头
     */
    var header: String? = null

    /**
     * 异常信息
     */
    var error: String? = null

    var requestBody: String? = null

    var responseBody: String? = null

}