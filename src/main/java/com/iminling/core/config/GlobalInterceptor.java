package com.iminling.core.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.iminling.core.config.argument.DefaultRequestDataReader;
import com.iminling.core.config.argument.RequestArgumentResolver;
import com.iminling.core.config.argument.RequestDataWrapper;
import com.iminling.core.filter.Filter;
import com.iminling.core.service.ILogService;
import com.iminling.core.util.ResponseWriter;
import com.iminling.core.util.ThreadContext;
import com.iminling.model.core.ClientInfo;
import com.iminling.model.core.LogRecord;
import com.iminling.model.exception.AuthorizeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class GlobalInterceptor implements HandlerInterceptor {

    private final String AUTHORIZATION = "Authorization";

    private ExecutorService executorService = new ThreadPoolExecutor(1, 2,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "log writer " + threadNumber.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private final DefaultRequestDataReader defaultRequestDataReader;
    private final List<Filter> filters;
    private final List<ILogService> logServices;
    private final boolean enableArgumentLog;

    public GlobalInterceptor(List<Filter> filters,
                             List<ILogService> logServices,
                             DefaultRequestDataReader defaultRequestDataReader,
                             boolean enableArgumentLog) {
        this.filters = filters;
        this.logServices = logServices;
        this.defaultRequestDataReader = defaultRequestDataReader;
        this.enableArgumentLog = enableArgumentLog;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 过滤非controller的请求
        if (!handler.getClass().isAssignableFrom(HandlerMethod.class)
                || "/error".equalsIgnoreCase(request.getRequestURI())
                || "/favicon.ico".equalsIgnoreCase(request.getRequestURI())) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        initClientInfo(request);
        initLogRecord(request, handlerMethod);
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(request);
        RequestDataWrapper requestDataWrapper;
        if (defaultRequestDataReader.canRead(inputMessage)) {
            requestDataWrapper = new RequestDataWrapper(true);
            JsonNode read = defaultRequestDataReader.read(inputMessage, handlerMethod);
            requestDataWrapper.parseJsonNode(read);
            if (read != null) {
                ThreadContext.getLogRecord().setParam(read.toString());
                if (enableArgumentLog)
                    log.info("url:{}, 参数：{}", request.getRequestURI(), read.toString());
            }
        } else {
            String queryString = request.getQueryString();
            requestDataWrapper = new RequestDataWrapper(false);
            ThreadContext.getLogRecord().setParam(queryString);
            log.info("url:{}, 参数：{}", request.getRequestURI(), queryString);
        }
        request.setAttribute(RequestArgumentResolver.REQUEST_DATA_KEY, requestDataWrapper);
        for (Filter filter : filters) {
            try {
                filter.doFilter(handlerMethod, request);
            } catch (AuthorizeException ex) {
                ResponseWriter.write(response, ex);
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        LogRecord logRecord = ThreadContext.getLogRecord();
        if (Objects.nonNull(logRecord)) {
            long executeTime = System.currentTimeMillis() - logRecord.getRequestTime();
            logRecord.setExecuteTime(executeTime);
            logRecord.setResponseStatus(response.getStatus());
            writeLog();
        }
        ThreadContext.clear();
    }

    /**
     * 初始化ClientInfo
     * @param request request
     */
    private void initClientInfo(HttpServletRequest request) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setRequestIp(getRemoteIpAddr(request));
        clientInfo.setContextPath(request.getContextPath());
        clientInfo.setServletPath(request.getServletPath());
        clientInfo.setPath(getPath(request));
        clientInfo.setToken(getToken(request));
        ThreadContext.setClientInfo(clientInfo);
    }

    /**
     * 初始化logRecord
     * @param request request
     * @param handlerMethod handlerMethod
     */
    private void initLogRecord(HttpServletRequest request, HandlerMethod handlerMethod) {
        LogRecord logRecord = new LogRecord();
        logRecord.setRequestTime(System.currentTimeMillis());
        logRecord.setIp(getRemoteIpAddr(request));
        logRecord.setUri(request.getRequestURI());
        /*ApiDesc annotation1 = handlerMethod.getBeanType().getAnnotation(ApiDesc.class);
        if (Objects.nonNull(annotation1)) {
            logRecord.setModule(annotation1.module().getValue());
        }*/
        ThreadContext.setLogRecord(logRecord);
    }

    /**
     * 获取token
     * @param request 请求
     * @return token
     */
    private String getToken(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION);
    }

    /**
     * 获取path
     * @param request 请求
     * @return path
     */
    private String getPath(HttpServletRequest request) {
        String path = request.getHeader("path");
        if (StringUtils.isNotEmpty(path)) {
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        return path;
    }

    /**
     * 获取真实ip
     * @param request 请求
     * @return 真实ip
     */
    private String getRemoteIpAddr(HttpServletRequest request) {
        String unknown = "unknown";
        String remoteIpAddr = unknown;
        try {
            String ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || unknown.equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || unknown.equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || unknown.equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)){
                    ipAddress = InetAddress.getLocalHost().getHostAddress();
                }
            } if (ipAddress != null && ipAddress.indexOf(',') > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(','));
            }
            remoteIpAddr = ipAddress;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return remoteIpAddr;
    }

    /**
     * 保存日志
     */
    private void writeLog() {
        LogRecord logRecord = ThreadContext.getLogRecord();
        if (StringUtils.isNotEmpty(logRecord.getDescription())) {
            logServices.stream().forEach(log -> {
                executorService.execute(() -> {
                    log.saveLog(logRecord);
                });
            });
        }
    }

}
