package com.iminling.core.config.exception;

import com.iminling.model.common.ResultModel;
import com.iminling.model.exception.AuthorizeException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 主要通途: 快速构建错误信息.
 * 设计说明:
 * 1.提供常用的API(例如#getError,#getHttpStatus),让控制器/处理器更专注于业务开发!!
 * 2.从配置文件读取错误配置信息,例如是否打印堆栈轨迹等。
 * 3.添加@Order注解和实现HandlerExceptionResolver接口是为了在获取异常。
 *
 * https://www.jianshu.com/p/3998ea8b53a8
 * @author konghang
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class ErrorInfoBuilder implements HandlerExceptionResolver, Ordered {

    private final Logger logger = LoggerFactory.getLogger(ErrorInfoBuilder.class);

    /**
     * 错误KEY
     */
    private final static String ERROR_NAME = "yslao.error";

    /**
     * 错误配置(ErrorConfiguration)
     */
    private ErrorProperties errorProperties;

    public ErrorProperties getErrorProperties() {
        return errorProperties;
    }

    public void setErrorProperties(ErrorProperties errorProperties) {
        this.errorProperties = errorProperties;
    }

    /**
     * 错误构造器 (Constructor) 传递配置属性：server.xx -> server.error.xx
     */
    public ErrorInfoBuilder(ServerProperties serverProperties) {
        this.errorProperties = serverProperties.getError();
    }

    /**
     * 构建错误信息.
     */
    public ResultModel getErrorInfo(HttpServletRequest request, HttpServletResponse response) {
        return getErrorInfo(request, response, getError(request));
    }

    /**
     * 构建错误信息.
     */
    public ResultModel getErrorInfo(HttpServletRequest request, HttpServletResponse response, Throwable error) {
        StringBuilder sb = new StringBuilder();
        if (error instanceof BindException){
            ((BindException) error).getBindingResult().getFieldErrors().stream()
                    .forEach(one -> sb.append("字段：").append(one.getField()).append(",").append(one.getDefaultMessage()).append(System.lineSeparator()));
        } else if (error instanceof AuthorizeException){
            sb.append(error.getMessage());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, HttpStatus.FORBIDDEN.value());
        } else {
            sb.append(error.getMessage());
            logger.error(error.getMessage());
            // logger.error(getStackTraceInfo(error, isIncludeStackTrace(request)));
        }
        String message = sb.toString();
        return ResultModel.isFail(getHttpStatus(request).value())
                .setMessage(message)
                .setData(getHttpStatus(request).getReasonPhrase());
    }

    /**
     * 获取错误.(Error/Exception)
     *
     * 获取方式：通过Request对象获取(Key="javax.servlet.error.exception").
     *
     * @see org.springframework.boot.web.servlet.error.DefaultErrorAttributes addErrorDetails
     */
    public Throwable getError(HttpServletRequest request) {
        //根据HandlerExceptionResolver接口方法来获取错误.
        Throwable error = (Throwable) request.getAttribute(ERROR_NAME);
        //根据Request对象获取错误.
        if (error == null) {
            error = (Throwable) request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
        }
        //当获取错误非空,取出RootCause.
        if (error != null) {
            while (error instanceof ServletException && error.getCause() != null) {
                error = error.getCause();
            }
        }//当获取错误为null,此时我们设置错误信息即可.
        else {
            String message = (String) request.getAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE);
            if (StringUtils.isEmpty(message)) {
                HttpStatus status = getHttpStatus(request);
                message = "Unknown Exception With " + status.value() + " " + status.getReasonPhrase();
            }
            error = new Exception(message);
        }
        return error;
    }

    /**
     * 获取通信状态(HttpStatus)
     *
     * @see org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController getStatus
     */
    public HttpStatus getHttpStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);
        try {
            return statusCode != null ? HttpStatus.valueOf(statusCode) : HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * 获取堆栈轨迹(StackTrace)
     *
     * @see org.springframework.boot.web.servlet.error.DefaultErrorAttributes# addStackTrace
     */
    public String getStackTraceInfo(Throwable error, boolean flag) {
        if (!flag) {
            return "omitted";
        }
        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        stackTrace.flush();
        return stackTrace.toString();
    }

    /**
     * 判断是否包含堆栈轨迹.(isIncludeStackTrace)
     *
     * @see org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController isIncludeStackTrace
     */
    public boolean isIncludeStackTrace(HttpServletRequest request) {

        // 读取错误配置(server.error.include-stacktrace=NEVER)
        IncludeStacktrace includeStacktrace = errorProperties.getIncludeStacktrace();

        // 情况1：若IncludeStacktrace为ALWAYS
        if (includeStacktrace == IncludeStacktrace.ALWAYS) {
            return true;
        }
        // 情况2：若请求参数含有trace
        if (includeStacktrace == IncludeStacktrace.ON_TRACE_PARAM) {
            String parameter = request.getParameter("trace");
            return parameter != null && !"false".equals(parameter.toLowerCase());
        }
        // 情况3：其它情况
        return false;
    }



    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 保存错误/异常.
     *
     * @see org.springframework.web.servlet.DispatcherServlet processHandlerException 进行选举HandlerExceptionResolver
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        request.setAttribute(ERROR_NAME, ex);
        return null;
    }
}
