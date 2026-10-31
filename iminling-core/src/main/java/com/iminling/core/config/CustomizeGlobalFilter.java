package com.iminling.core.config;

import com.iminling.core.log.*;
import com.iminling.core.util.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.*;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class CustomizeGlobalFilter extends OncePerRequestFilter implements Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(CustomizeGlobalFilter.class);

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        ClientInfo client = new ClientInfo();
        client.setToken(request.getHeader("Authorization"));
        client.setRequestIp(getRemoteIpAddr(request));
        client.setRequestTime(System.currentTimeMillis());
        ThreadContext.setClientInfo(client);
        boolean log =
                LogUtils.containsMethod(request.getMethod())
                        && LogUtils.canLog(request.getRequestURI());
        HttpServletRequest req = request;
        HttpServletResponse res = response;
        if (log) {
            if (!(req instanceof ContentCachingRequestWrapper))
                req = new ContentCachingRequestWrapper(req, Math.max(req.getContentLength(), 1024));
            if (!(res instanceof ContentCachingResponseWrapper))
                res = new ContentCachingResponseWrapper(res);
            RequestLog requestLog = new RequestLog();
            requestLog.wrap(req, null);
            LOG.info("{}", requestLog);
        }
        long start = System.currentTimeMillis();
        Exception error = null;
        try {
            chain.doFilter(req, res);
        } catch (IOException | ServletException | RuntimeException e) {
            error = e;
            throw e;
        } finally {
            try {
                if (log) {
                    var requestWrapper =
                            WebUtils.getNativeRequest(req, ContentCachingRequestWrapper.class);
                    var responseWrapper =
                            WebUtils.getNativeResponse(res, ContentCachingResponseWrapper.class);
                    String requestBody =
                            requestWrapper == null
                                    ? ""
                                    : new String(
                                            requestWrapper.getContentAsByteArray(),
                                            StandardCharsets.UTF_8);
                    String responseBody =
                            responseWrapper == null
                                    ? ""
                                    : new String(
                                            responseWrapper.getContentAsByteArray(),
                                            StandardCharsets.UTF_8);
                    try {
                        ResponseLog responseLog = new ResponseLog();
                        responseLog.wrap(
                                req,
                                res,
                                requestBody,
                                responseBody,
                                error == null ? null : error.getMessage(),
                                System.currentTimeMillis() - start);
                        LOG.info("{}", responseLog);
                    } finally {
                        if (responseWrapper != null) responseWrapper.copyBodyToResponse();
                    }
                }
            } finally {
                ThreadContext.clear();
            }
        }
    }

    private String getRemoteIpAddr(HttpServletRequest request) {
        for (String name :
                new String[] {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP"}) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value))
                return value.split(",")[0];
        }
        String ip = request.getRemoteAddr();
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (IOException ignored) {
                return ip;
            }
        }
        return ip;
    }
}
