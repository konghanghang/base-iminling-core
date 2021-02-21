package com.iminling.core.filter.impl;

import com.iminling.core.filter.AuthFilter;
import com.iminling.model.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * 默认auth实现
 * @author yslao@outlook.com
 */
public class DefaultAuthFilter implements AuthFilter {

    private final Logger logger = LoggerFactory.getLogger(DefaultAuthFilter.class);

    @Override
    public void doFilter(HandlerMethod handlerMethod, HttpServletRequest request) throws BizException {
    }

    @Override
    public int getOrder() {
        return -99998;
    }
}
