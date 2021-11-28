package com.iminling.core.config.filter.impl

import com.iminling.core.config.filter.LoginFilter
import org.springframework.web.method.HandlerMethod
import javax.servlet.http.HttpServletRequest

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
class DefaultLoginFilter: LoginFilter {
    override fun doFilter(handlerMethod: HandlerMethod, request: HttpServletRequest) {

    }

    override fun getOrder(): Int {
        return 1
    }
}