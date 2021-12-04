package com.iminling.core.log

import com.iminling.common.json.JsonUtil
import com.iminling.core.util.LogUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class ResponseLog(var type: String = "spring") {

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

    fun wrap(request: HttpServletRequest, response: HttpServletResponse, requestBody: String, responseBody: String, error: String?, time: Long) {
        this.uri = request.requestURI
        this.httpMethod = request.method
        this.respStatus = response.status
        this.status = response.status
        this.time = time
        this.message = ""
        this.header = JsonUtil.obj2Str(extractHeaderToMap(response))
        this.error = error
        this.requestBody = if(requestBody.length >= 500) requestBody.substring(0, 500) else requestBody
        this.responseBody = if(responseBody.length >= 500) responseBody.substring(0, 500) else responseBody
    }

    private fun extractHeaderToMap(response: HttpServletResponse): Map<String, String> {
        val headerNames = response.headerNames ?: return mutableMapOf()
        val headerMap = mutableMapOf<String, String>()
        for (header in headerNames) {
            if (!LogUtils.containsHeader(header)) {
                headerMap[header] = response.getHeader(header)
            }
        }
        return headerMap
    }

    override fun toString(): String {
        return "Log_1 ${JsonUtil.obj2Str(this)}"
    }

}