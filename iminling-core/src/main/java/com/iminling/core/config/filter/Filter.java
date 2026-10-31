package com.iminling.core.config.filter;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

public interface Filter extends Ordered {
    void doFilter(HandlerMethod handler, HttpServletRequest request);

    default <T extends Annotation> T getAnnotation(HandlerMethod handler, Class<T> type) {
        T result = AnnotatedElementUtils.findMergedAnnotation(handler.getMethod(), type);
        return result != null
                ? result
                : AnnotatedElementUtils.findMergedAnnotation(handler.getBeanType(), type);
    }
}
