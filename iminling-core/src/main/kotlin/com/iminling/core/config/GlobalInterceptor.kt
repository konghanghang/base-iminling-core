package com.iminling.core.config

import com.iminling.common.json.JsonUtil
import com.iminling.core.annotation.EnableResolve
import com.iminling.core.config.argument.DefaultRequestDataReader
import com.iminling.core.config.argument.RequestDataWrapper
import com.iminling.core.config.filter.Filter
import com.iminling.core.constant.ResolveStrategy
import com.iminling.core.constant.StringEnum
import com.iminling.core.util.LogUtils
import com.iminling.core.util.ThreadContext
import org.slf4j.LoggerFactory
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.ContentCachingResponseWrapper
import org.springframework.web.util.WebUtils
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class GlobalInterceptor(
    var defaultRequestDataReader: DefaultRequestDataReader,
    var filters: List<Filter>,
    var environment: ConfigurableEnvironment
) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(GlobalInterceptor::class.java)

    /*private ExecutorService executorService = new ThreadPoolExecutor(1, 2,
    60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(1000),
    new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "log writer " + threadNumber.getAndIncrement());
        }
    },
    new ThreadPoolExecutor.DiscardOldestPolicy());*/

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // 过滤非controller的请求
        if (!handler.javaClass.isAssignableFrom(HandlerMethod::class.java)
            || LogUtils.canLog(request.requestURI)
        ) {
             return true
        }
        val handlerMethod = handler as HandlerMethod
        var enableResolve = handlerMethod.getMethodAnnotation(EnableResolve::class.java) ?: handlerMethod.beanType.getAnnotation(EnableResolve::class.java)
        if (Objects.nonNull(enableResolve) &&
            (enableResolve.value == ResolveStrategy.ALL || enableResolve.value == ResolveStrategy.ARGUMENTS)
        ) {
            handlerCustomizeArgument(request, handlerMethod)
        }
        return super.preHandle(request, response, handler)
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if (!handler.javaClass.isAssignableFrom(HandlerMethod::class.java)
            || !LogUtils.canLog(request.requestURI)
        ) {
            return
        }
        // handlerArgument(request)
        handlerResult(request, response)
        ThreadContext.clear()
    }

    /**
     * 处理自定义参数处理器解析
     * @param request   请求
     * @param handlerMethod handlerMethod
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun handlerCustomizeArgument(request: HttpServletRequest, handlerMethod: HandlerMethod) {
        val inputMessage = ServletServerHttpRequest(request)
        val requestDataWrapper = RequestDataWrapper(false)
        request.setAttribute(StringEnum.REQUEST_DATA_KEY.desc, requestDataWrapper)
        if (defaultRequestDataReader.canRead(inputMessage)) {
            requestDataWrapper.canRead = true
            val read = defaultRequestDataReader.read(inputMessage, handlerMethod)
            requestDataWrapper.parseJsonNode(read)
            var body = read.toString()
        }
        val parameterMap = request.parameterMap
        var param = JsonUtil.obj2Str(parameterMap)
    }

    /**
     * 处理结果打印
     * @param request   request
     * @param response  response
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun handlerResult(request: HttpServletRequest, response: HttpServletResponse) {
        WebUtils.getNativeResponse(
            response,
            ContentCachingResponseWrapper::class.java
        )?.let {
            val buf = it.contentAsByteArray
            if (buf.isNotEmpty()) {
                val result = String(buf, 0, buf.size, Charsets.UTF_8)
                logger.info("url:{}, result:{}", request.requestURI, result)
            }
        }
    }

    /**
     * 处理参数打印
     * @param request   request
     * @throws UnsupportedEncodingException
     */
    /*private fun handlerArgument(request: HttpServletRequest) {
        if (Objects.isNull(ThreadContext.getLogRecord())) {
            return
        }
        ThreadContext.getLogRecord()?.let {
            WebUtils.getNativeRequest(
                request,
                ContentCachingRequestWrapper::class.java
            )?.let {
                val buf = it.contentAsByteArray
                if (buf.isNotEmpty()) {
                    val requestBody = String(buf, 0, buf.size, Charsets.UTF_8)
                    ThreadContext.getLogRecord()!!.body = requestBody
                }
            }
            val parameterMap = request.parameterMap
            ThreadContext.getLogRecord()!!.param = JsonUtil.obj2Str(parameterMap)
            logger.info(
                "url:{}, queryString:{}, body：{}", request.requestURI, ThreadContext.getLogRecord()!!
                    .param, ThreadContext.getLogRecord()!!.body
            )
        }
    }*/

}