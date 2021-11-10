package com.iminling.core.config;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.iminling.common.json.JsonUtil;
import com.iminling.core.ApplicationConstant;
import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.annotation.EnableResolve.ResolveStrategy;
import com.iminling.core.config.argument.DefaultRequestDataReader;
import com.iminling.core.config.argument.GlobalArgumentResolver;
import com.iminling.core.config.argument.RequestDataWrapper;
import com.iminling.core.filter.Filter;
import com.iminling.core.service.ILogService;
import com.iminling.core.util.IpUtils;
import com.iminling.core.util.LogUtils;
import com.iminling.core.util.ResponseWriter;
import com.iminling.core.util.ThreadContext;
import com.iminling.model.core.ClientInfo;
import com.iminling.model.core.LogRecord;
import com.iminling.model.exception.AuthorizeException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

@Slf4j
@RequiredArgsConstructor
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
    private final ConfigurableEnvironment environment;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 过滤非controller的请求
        if (!handler.getClass().isAssignableFrom(HandlerMethod.class)
                || LogUtils.Companion.canLog(request.getRequestURI())) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        initClientInfo(request);
        EnableResolve enableResolve = handlerMethod.getMethodAnnotation(EnableResolve.class);
        if (Objects.isNull(enableResolve)) {
            enableResolve = handlerMethod.getBeanType().getAnnotation(EnableResolve.class);
        }
        if (Objects.nonNull(enableResolve) &&
            (enableResolve.value().equals(ResolveStrategy.ALL) || enableResolve.value().equals(ResolveStrategy.ARGUMENTS))) {
            handlerCustomizeArgument(request, handlerMethod);
        }
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

    /**
     * 处理自定义参数处理器解析
     * @param request   请求
     * @param handlerMethod handlerMethod
     * @throws IOException
     */
    private void handlerCustomizeArgument(HttpServletRequest request, HandlerMethod handlerMethod) throws IOException {
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(request);
        RequestDataWrapper requestDataWrapper = new RequestDataWrapper(false);
        request.setAttribute(GlobalArgumentResolver.REQUEST_DATA_KEY, requestDataWrapper);
        if (defaultRequestDataReader.canRead(inputMessage)) {
            requestDataWrapper.setCanRead(true);
            JsonNode read = defaultRequestDataReader.read(inputMessage, handlerMethod);
            requestDataWrapper.parseJsonNode(read);
            if (read != null) {
                ThreadContext.getLogRecord().setBody(read.toString());
            }
        }
        Map<String, String[]> parameterMap = request.getParameterMap();
        ThreadContext.getLogRecord().setParam(JsonUtil.obj2Str(parameterMap));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        if (!handler.getClass().isAssignableFrom(HandlerMethod.class)
            || !LogUtils.Companion.canLog(request.getRequestURI())) {
            return;
        }
        boolean enableArgumentLog = environment.getProperty(ApplicationConstant.KEY_LOGGER_ARGUMENTS, Boolean.class, false);
        if (enableArgumentLog) {
            handlerArgument(request);
        }
        boolean enableResultLog = environment.getProperty(ApplicationConstant.KEY_LOGGER_RESULT, Boolean.class, false);
        if (enableResultLog) {
            handlerResult(request, response);
        }
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
     * 处理结果打印
     * @param request   request
     * @param response  response
     * @throws IOException
     */
    private void handlerResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (Objects.nonNull(responseWrapper)) {
            responseWrapper.setCharacterEncoding(StandardCharsets.UTF_8.name());
            byte[] buf = responseWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String result = new String(buf, 0, buf.length, responseWrapper.getCharacterEncoding());
                log.info("url:{}, result:{}", request.getRequestURI(), result);
            }
        }
    }

    /**
     * 处理参数打印
     * @param request   request
     * @throws UnsupportedEncodingException
     */
    private void handlerArgument(HttpServletRequest request) throws UnsupportedEncodingException {
        if (Objects.isNull(ThreadContext.getLogRecord())) {
            return;
        }
        ContentCachingRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (Objects.nonNull(requestWrapper)) {
            requestWrapper.setCharacterEncoding(StandardCharsets.UTF_8.name());
            byte[] buf = requestWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String requestBody = new String(buf, 0, buf.length, requestWrapper.getCharacterEncoding());
                ThreadContext.getLogRecord().setBody(requestBody);
            }
        }
        Map<String, String[]> parameterMap = request.getParameterMap();
        ThreadContext.getLogRecord().setParam(JsonUtil.obj2Str(parameterMap));
        log.info("url:{}, queryString:{}, body：{}", request.getRequestURI(), ThreadContext.getLogRecord().getParam(), ThreadContext.getLogRecord().getBody());
    }

    /**
     * 初始化ClientInfo
     * @param request request
     */
    private void initClientInfo(HttpServletRequest request) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setRequestIp(IpUtils.getRemoteIpAddr(request));
        clientInfo.setContextPath(request.getContextPath());
        clientInfo.setServletPath(request.getServletPath());
        clientInfo.setPath(getPath(request));
        clientInfo.setToken(getToken(request));
        ThreadContext.setClientInfo(clientInfo);
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
        if (StrUtil.isNotEmpty(path)) {
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        return path;
    }

    /**
     * 保存日志
     */
    private void writeLog() {
        LogRecord logRecord = ThreadContext.getLogRecord();
        if (StrUtil.isNotEmpty(logRecord.getDescription())) {
            logServices.stream().forEach(log -> {
                executorService.execute(() -> {
                    log.saveLog(logRecord);
                });
            });
        }
    }

}
