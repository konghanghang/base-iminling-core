package com.iminling.core.filter;

import com.iminling.core.exception.BizException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;

public interface Filter extends Ordered {

    void doFilter(HandlerMethod handlerMethod, HttpServletRequest request) throws BizException;

    default <T extends Annotation> T getAnnotation(HandlerMethod handlerMethod, Class<T> clazz) {
        T annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), clazz);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(clazz);
        }
        return annotation;
    }
}
