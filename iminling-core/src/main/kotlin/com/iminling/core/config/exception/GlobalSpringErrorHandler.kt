package com.iminling.core.config.exception

import cn.hutool.core.collection.CollectionUtil
import com.iminling.core.notice.SendNoticeService
import org.slf4j.LoggerFactory
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.util.ErrorHandler

/**
 * 自定义异常处理器
 * @author  yslao@outlook.com
 * @since  2022/10/9
 */
class GlobalSpringErrorHandler:ErrorHandler {

    private val logger = LoggerFactory.getLogger(GlobalSpringErrorHandler::class.java)

    private var environment: ConfigurableEnvironment
    private var sendNoticeServices: MutableList<SendNoticeService> = mutableListOf()

    constructor(environment: ConfigurableEnvironment, sendNoticeServices: List<SendNoticeService>) {
        this.environment = environment
        this.sendNoticeServices.addAll(sendNoticeServices)
    }

    override fun handleError(t: Throwable) {
        logger.error("Unexpected error occurred in scheduled task", t)
        val enable = environment.getProperty("errorHandler.enable", Boolean.javaClass, false) as Boolean
        if (!enable) {
            return
        }
        var users = environment.getProperty("error.notice.user", List::class.java)
        if (CollectionUtil.isEmpty(users)) {
            return
        }
        sendNoticeServices.forEach { it ->
            var service = it
            users!!.forEach {
                service.sendNotice(it.toString(), t.stackTraceToString())
            }
        }
    }
}