package com.iminling.core.log

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class RequestLog {

    /**
     * 请求URI
     */
    var uri: String? = null

    /**
     * 请求http消息类型
     */
    var httpMethod: String? = null

    /**
     * 请求头
     */
    var header: String? = null

    /**
     * 请求参数
     */
    var param: String? = null

    /**
     * 请求消息体
     */
    var body: String? = null

    var clientIp: String? = null

}