package com.iminling.core.config.exception;

import com.iminling.common.json.JsonUtil;
import com.iminling.model.common.MessageCode;
import com.iminling.model.common.ResultModel;
import com.iminling.model.exception.AuthorizeException;
import com.iminling.model.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BindException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel validExceptionHandler(BindException e) {
        List<FieldError> fieldErrors=e.getBindingResult().getFieldErrors();
        Map<String, String> map = new HashMap<>();
        for (FieldError error:fieldErrors){
            map.put(error.getField(),error.getDefaultMessage());
        }
        return ResultModel.isFail(JsonUtil.obj2Str(map));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        return ResultModel.isFail(fieldErrors.get(0).getDefaultMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ResultModel noHandlerFoundExceptionHandler(NoHandlerFoundException e, HttpServletRequest request) {
        return ResultModel.isFail(String.format("url:%s not found", request.getServletPath()), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(AuthorizeException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ResultModel authorizeExceptionHandler(AuthorizeException e, HttpServletRequest request, HttpServletResponse response) {
        logger.error("AuthorizeException, url:{}, method:{}, code:{}, message:{}", request.getServletPath(), request.getMethod(),
                e.getMessageCode().getCode(), e.getMessageCode().getMessage());
        response.setStatus(e.getMessageCode().getCode());
        return ResultModel.isFail(e.getMessageCode());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e, HttpServletRequest request) {
        return ResultModel.isFail(e.getParameterName() + "不能为空");
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel validationExceptionHandler(ValidationException e, HttpServletRequest request) {
        logger.error("ValidationException, url:{}, method:{}, message:{}", request.getRequestURI(), request.getMethod(), e.getMessage());
        return ResultModel.isFail(e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel baseExceptionHandler(BizException e) {
        if (Objects.nonNull(e.getMessageCode())){
            return ResultModel.isFail(e.getMessageCode());
        } else {
            return ResultModel.isFail(e.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    //@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultModel exceptionHandler(Exception e, HttpServletRequest request) {
        logger.error("Exception, url:{}, message:{}", request.getRequestURI(), e.getMessage(), e);
        return ResultModel.isFail(MessageCode.RESULT_FAIL);
    }

}
