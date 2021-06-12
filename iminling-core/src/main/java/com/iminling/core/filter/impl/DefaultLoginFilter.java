package com.iminling.core.filter.impl;

import com.iminling.core.filter.LoginFilter;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * 默认login实现
 * @author yslao@outlook.com
 */
public class DefaultLoginFilter implements LoginFilter {

    @Override
    public void doFilter(HandlerMethod handlerMethod, HttpServletRequest request) {
    }

    @Override
    public int getOrder() {
        return -99999;
    }
}
