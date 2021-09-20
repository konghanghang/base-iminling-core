package com.iminling.core.filter;

import com.iminling.core.util.IpUtils;
import com.iminling.core.util.ThreadContext;
import com.iminling.model.core.LogRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

/**
 * 定义filter，包装HttpServletRequest和HttpServletResponse
 * 分别为ContentCachingRequestWrapper和ContentCachingResponseWrapper
 */
public class CustomizeGlobalFilter extends OncePerRequestFilter implements Ordered {

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;
        HttpServletResponse responseToUse = response;
        initLogRecord(request);
        if (!isFirstRequest || (request instanceof ContentCachingRequestWrapper)) {
            return;
        }
        if (!"PUT".equalsIgnoreCase(request.getMethod()) && !"POST".equalsIgnoreCase(request.getMethod())
            && !"DELETE".equalsIgnoreCase(request.getMethod()) && !"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        if (isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request, request.getContentLength()>=0 ? request.getContentLength() : 1024);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        }
        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(responseToUse, ContentCachingResponseWrapper.class);
            if (Objects.nonNull(responseWrapper)) {
                // Do not forget this line after reading response content or actual response will be empty!
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    /**
     * 初始化logRecord
     * @param request request
     */
    private void initLogRecord(HttpServletRequest request) {
        LogRecord logRecord = new LogRecord();
        logRecord.setRequestTime(System.currentTimeMillis());
        logRecord.setIp(IpUtils.getRemoteIpAddr(request));
        logRecord.setUri(request.getRequestURI());
        ThreadContext.setLogRecord(logRecord);
    }
}
