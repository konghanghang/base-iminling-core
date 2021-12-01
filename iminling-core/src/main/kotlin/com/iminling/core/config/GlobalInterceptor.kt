package com.iminling.core.config

import com.iminling.core.annotation.EnableResolve
import com.iminling.core.config.argument.DefaultRequestDataReader
import com.iminling.core.config.argument.RequestDataWrapper
import com.iminling.core.config.filter.Filter
import com.iminling.core.constant.ResolveStrategy
import com.iminling.core.constant.StringEnum
import com.iminling.core.exception.AuthorizeException
import com.iminling.core.util.LogUtils
import com.iminling.core.util.ResponseWriter
import com.iminling.core.util.ThreadContext
import org.slf4j.LoggerFactory
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.io.IOException
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
            || !LogUtils.canLog(request.requestURI)
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
        for (filter in filters) {
            try {
                filter.doFilter(handlerMethod, request)
            } catch (ex: AuthorizeException) {
                ResponseWriter.write(response, ex)
                return false
            }
        }
        return true
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
            // var body = read.toString()
        }
    }

}