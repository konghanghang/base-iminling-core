package com.iminling.core.config

import cn.hutool.core.util.StrUtil
import com.iminling.common.json.JsonUtil
import com.iminling.core.constant.StringEnum
import com.iminling.core.log.ClientInfo
import com.iminling.core.util.LogUtils
import com.iminling.core.log.RequestLog
import com.iminling.core.log.ResponseLog
import com.iminling.core.util.ThreadContext
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import org.springframework.web.util.WebUtils
import java.net.InetAddress
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class CustomizeGlobalFilter: OncePerRequestFilter(), Ordered {

    private val logger = LoggerFactory.getLogger(CustomizeGlobalFilter::class.java)

    override fun getOrder(): Int {
        return -1
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val isFirstRequest = !isAsyncDispatch(request)
        initClientInfo(request)
        var requestToUse = request
        var responseToUse = response
        if (!isFirstRequest || request is ContentCachingRequestWrapper) {
            return
        }
        var flag = false
        if (LogUtils.containsMethod(request.method) || !LogUtils.canLog(request.requestURI)) {
            flag = true
            if (isFirstRequest && request !is ContentCachingRequestWrapper) {
                requestToUse = ContentCachingRequestWrapper(
                    request,
                    if (request.contentLength >= 0) request.contentLength else 1024
                )
            }
            if (response !is ContentCachingResponseWrapper) {
                responseToUse = ContentCachingResponseWrapper(response)
            }
        }
        var startTime = System.currentTimeMillis()
        var requestLog = RequestLog(StringEnum.SPRING.desc)
        requestLog.wrap(request, JsonUtil.obj2Str(request.getAttribute(StringEnum.REQUEST_DATA_KEY.desc)))
        logger.info("$requestLog")
        var error: Exception? = null
        try {
            filterChain.doFilter(requestToUse, responseToUse)
        } catch (e: Exception) {
            logger.error(e.message, e)
            error = e
            throw e
        } finally {
            if (flag) {
                try {
                    var requestBody = ""
                    WebUtils.getNativeRequest(
                        requestToUse,
                        ContentCachingRequestWrapper::class.java
                    )?.let {
                        val buf = it.contentAsByteArray
                        if (buf.isNotEmpty()) {
                            requestBody = String(buf, 0, buf.size, Charsets.UTF_8)
                        }
                    }
                    var responseBody = ""
                    WebUtils.getNativeResponse(
                        responseToUse,
                        ContentCachingResponseWrapper::class.java
                    )?.let {
                        val buf = it.contentAsByteArray
                        if (buf.isNotEmpty()) {
                            responseBody = String(buf, 0, buf.size, Charsets.UTF_8)
                        }
                        // Do not forget this line after reading response content or actual response will be empty!
                        it.copyBodyToResponse()
                    }
                    val endTime = System.currentTimeMillis()
                    var responseLog = ResponseLog(StringEnum.SPRING.desc)
                    responseLog.wrap(request, response, requestBody, responseBody, error?.message, endTime - startTime)
                    logger.info("$responseLog")
                } catch (e: Exception) {
                    logger.error(e.message, e)
                }
            }
        }
    }

    /**
     * 初始化ClientInfo
     * @param request request
     */
    private fun initClientInfo(request: HttpServletRequest) {
        val clientInfo = ClientInfo()
        clientInfo.token = request.getHeader(StringEnum.AUTHORIZATION.desc)
        clientInfo.requestIp = getRemoteIpAddr(request)
        ThreadContext.setClientInfo(clientInfo)
    }

    /**
     * 获取真实ip
     * @param request 请求
     * @return 真实ip
     */
    private fun getRemoteIpAddr(request: HttpServletRequest): String? {
        val unknown = "unknown"
        var remoteIpAddr: String? = unknown
        try {
            var ipAddress = request.getHeader("x-forwarded-for")
            if (StrUtil.isBlank(ipAddress) || unknown.equals(ipAddress, ignoreCase = true)) {
                ipAddress = request.getHeader("Proxy-Client-IP")
            }
            if (StrUtil.isBlank(ipAddress) || unknown.equals(ipAddress, ignoreCase = true)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP")
            }
            if (StrUtil.isBlank(ipAddress) || unknown.equals(ipAddress, ignoreCase = true)) {
                ipAddress = request.remoteAddr
                if ("127.0.0.1" == ipAddress || "0:0:0:0:0:0:0:1" == ipAddress) {
                    ipAddress = InetAddress.getLocalHost().hostAddress
                }
            }
            if (ipAddress != null && ipAddress.indexOf(',') > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(','))
            }
            remoteIpAddr = ipAddress
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
        return remoteIpAddr
    }

}