package com.iminling.core.config.exception;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.config.value.ResultModel;
import com.iminling.core.constant.MessageCode;
import com.iminling.core.exception.*;

import jakarta.servlet.http.*;
import jakarta.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel<?> validExceptionHandler(BindException error) {
        var fields = new LinkedHashMap<String, String>();
        error.getBindingResult()
                .getFieldErrors()
                .forEach(field -> fields.put(field.getField(), field.getDefaultMessage()));
        return ResultModel.fail(JsonUtil.obj2Str(fields));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel<?> methodArgumentNotValidExceptionHandler(
            MethodArgumentNotValidException error) {
        return ResultModel.fail(
                error.getBindingResult().getFieldErrors().stream()
                        .map(field -> String.valueOf(field.getDefaultMessage()))
                        .collect(Collectors.joining("、")));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultModel<?> noHandlerFoundExceptionHandler(
            Exception error, HttpServletRequest request) {
        return ResultModel.fail("url:" + request.getServletPath() + " not found", 404);
    }

    @ExceptionHandler(AuthorizeException.class)
    public ResultModel<?> authorizeExceptionHandler(
            AuthorizeException error, HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(error.getMessageCode().getCode());
        return ResultModel.fail(error.getMessageCode());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel<?> missingServletRequestParameterExceptionHandler(
            MissingServletRequestParameterException error) {
        return ResultModel.fail(error.getParameterName() + "不能为空");
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel<?> validationExceptionHandler(
            ValidationException error, HttpServletRequest request) {
        return ResultModel.fail(error.getMessage() == null ? "" : error.getMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel<?> baseExceptionHandler(BizException error) {
        return error.getMessageCode() == null
                ? ResultModel.fail(error.getMessage())
                : ResultModel.fail(error.getMessageCode());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel<?> exceptionHandler(Exception error, HttpServletRequest request) {
        LOG.error("Exception, url:{}", request.getRequestURI(), error);
        return ResultModel.fail(MessageCode.RESULT_FAIL);
    }
}
