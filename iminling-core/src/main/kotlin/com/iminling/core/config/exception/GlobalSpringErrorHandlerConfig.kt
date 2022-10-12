package com.iminling.core.config.exception

import com.iminling.core.notice.SendNoticeService
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.config.TaskManagementConfigUtils

/**
 * 只有在有@EnableScheduling注解的时候才会生成
 * @author  yslao@outlook.com
 * @since  2022/10/11
 */
@ConditionalOnBean(name = [TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME])
class GlobalSpringErrorHandlerConfig : ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 创建ConcurrentTaskScheduler，在ScheduledAnnotationBeanPostProcessor中会尝试获取
     */
    @Bean
    fun taskScheduler(
        environment: ConfigurableEnvironment,
        sendNoticeServices: List<SendNoticeService>
    ): ConcurrentTaskScheduler {
        var taskScheduler = ConcurrentTaskScheduler()
        taskScheduler.setErrorHandler(GlobalSpringErrorHandler(environment, sendNoticeServices))
        return taskScheduler
    }
}