package com.iminling.core.filter;

import com.iminling.core.annotation.LoginRequired;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;

public class DefaultLoginFilter implements LoginFilter {

    @Override
    public void doFilter(HandlerMethod handlerMethod, HttpServletRequest request) {
        LoginRequired loginRequired = getAnnotation(handlerMethod, LoginRequired.class);
        if (loginRequired != null) {

        }
    }

    @Override
    public int getOrder() {
        return -99999;
    }
}
