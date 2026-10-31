package com.iminling.core.config.exception;

import com.iminling.core.notice.SendNoticeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ErrorHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class GlobalSpringErrorHandler implements ErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalSpringErrorHandler.class);
    private final ConfigurableEnvironment environment;
    private final List<SendNoticeService> services;

    public GlobalSpringErrorHandler(
            ConfigurableEnvironment environment, List<SendNoticeService> services) {
        this.environment = environment;
        this.services = List.copyOf(services);
    }

    @Override
    public void handleError(Throwable error) {
        LOG.error("Unexpected error occurred in scheduled task", error);
        if (!environment.getProperty("errorHandler.enable", Boolean.class, false)) return;
        List<?> users = environment.getProperty("error.notice.user", List.class);
        if (users == null || users.isEmpty()) return;
        StringWriter stack = new StringWriter();
        error.printStackTrace(new PrintWriter(stack));
        for (SendNoticeService service : services)
            for (Object user : users) service.sendNotice(String.valueOf(user), stack.toString());
    }
}
