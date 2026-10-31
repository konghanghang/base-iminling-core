package com.iminling.core.config.filter.impl;

import com.iminling.core.config.filter.AuthFilter;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.method.HandlerMethod;

public class DefaultAuthFilter implements AuthFilter {
    @Override
    public void doFilter(HandlerMethod handler, HttpServletRequest request) {}

    @Override
    public int getOrder() {
        return 1;
    }
}
