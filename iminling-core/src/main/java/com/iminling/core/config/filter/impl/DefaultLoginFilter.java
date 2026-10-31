package com.iminling.core.config.filter.impl;

import com.iminling.core.config.filter.LoginFilter;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.method.HandlerMethod;

public class DefaultLoginFilter implements LoginFilter {
    @Override
    public void doFilter(HandlerMethod handler, HttpServletRequest request) {}

    @Override
    public int getOrder() {
        return 1;
    }
}
