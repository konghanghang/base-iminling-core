package com.iminling.core.log

import com.iminling.common.json.JsonUtil
import com.iminling.core.util.LogUtils
import com.iminling.core.util.ThreadContext
import sun.plugin2.util.PojoUtil.toJson
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class RequestLog(var type: String = "spring") {

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

    fun wrap(request: HttpServletRequest, requestBody: String?) {
        this.uri = request.requestURI
        this.httpMethod = request.method
        this.header = JsonUtil.obj2Str(extractHeaderToMap(request))
        this.param = JsonUtil.obj2Str(request.parameterMap)
        this.body = requestBody
        this.clientIp = ThreadContext.getClientInfo()!!.requestIp
    }

    private fun extractHeaderToMap(request: HttpServletRequest): Map<String, String> {
        val headerNames = request.headerNames ?: return mutableMapOf()
        val headerMap = mutableMapOf<String, String>()
        for (header in headerNames) {
            if (!LogUtils.containsHeader(header)) {
                headerMap[header] = request.getHeader(header)
            }
        }
        return headerMap
    }

    override fun toString(): String {
        return "Log_0 ${JsonUtil.obj2Str(this)}"
    }

}