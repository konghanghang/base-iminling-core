package com.iminling.core.config.exception

import com.google.common.base.Joiner
import com.iminling.common.json.JsonUtil
import com.iminling.core.config.value.ResultModel
import com.iminling.core.constant.MessageCode
import com.iminling.core.exception.AuthorizeException
import com.iminling.core.exception.BizException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.ValidationException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BindException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun validExceptionHandler(exception: BindException) : ResultModel<*> {
        val fieldErrors: List<FieldError> = exception.bindingResult.fieldErrors
        val map: MutableMap<String, String?> = HashMap()
        for (error in fieldErrors) {
            map[error.field] = error.defaultMessage
        }
        return ResultModel.fail(JsonUtil.obj2Str(map))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun methodArgumentNotValidExceptionHandler(e: MethodArgumentNotValidException): ResultModel<*> {
        val fieldErrors = e.bindingResult.fieldErrors.map { it.defaultMessage }
        return ResultModel.fail(Joiner.on("、").join(fieldErrors))
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun noHandlerFoundExceptionHandler(e: NoHandlerFoundException?, request: HttpServletRequest): ResultModel<*> {
        return ResultModel.fail(String.format("url:%s not found", request.servletPath), HttpStatus.NOT_FOUND.value())
    }

    @ExceptionHandler(AuthorizeException::class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    fun authorizeExceptionHandler(
        e: AuthorizeException,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResultModel<*> {
        logger.error(
            "AuthorizeException, url:{}, method:{}, code:{}, message:{}", request.servletPath, request.method,
            e.messageCode.code, e.messageCode.message
        )
        response.status = e.messageCode.code
        return ResultModel.fail(e.messageCode)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun missingServletRequestParameterExceptionHandler(
        e: MissingServletRequestParameterException
    ): ResultModel<*> {
        return ResultModel.fail(e.parameterName + "不能为空")
    }

    @ExceptionHandler(ValidationException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun validationExceptionHandler(e: ValidationException, request: HttpServletRequest): ResultModel<*> {
        logger.error(
            "ValidationException, url:{}, method:{}, message:{}",
            request.requestURI,
            request.method,
            e.message
        )
        return ResultModel.fail(e.message ?: "")
    }

    @ExceptionHandler(BizException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun baseExceptionHandler(e: BizException): ResultModel<*> {
        return if (Objects.nonNull(e.messageCode)) {
            ResultModel.fail(e.messageCode ?: MessageCode.RESULT_FAIL)
        } else {
            ResultModel.fail(e.message ?: "")
        }
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun exceptionHandler(e: Exception, request: HttpServletRequest): ResultModel<*> {
        logger.error("Exception, url:{}, message:{}", request.requestURI, e.message, e)
        return ResultModel.fail(MessageCode.RESULT_FAIL)
    }

}