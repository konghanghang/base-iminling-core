package com.iminling.core.config.exception;

import com.iminling.core.notice.SendNoticeService;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
public class GlobalSpringErrorHandlerConfig {
    @Bean
    @ConditionalOnMissingBean(TaskScheduler.class)
    public ThreadPoolTaskScheduler taskScheduler(
            ConfigurableEnvironment environment, List<SendNoticeService> services) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("iminling-scheduled-");
        scheduler.setErrorHandler(new GlobalSpringErrorHandler(environment, services));
        return scheduler;
    }
}
