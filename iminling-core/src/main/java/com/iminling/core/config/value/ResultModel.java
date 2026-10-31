package com.iminling.core.config.value;

import com.iminling.core.constant.MessageCode;

public class ResultModel<T> {
    private int code;
    private T data;
    private String message;

    public ResultModel() {}

    public ResultModel(MessageCode code) {
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static ResultModel<Object> ok() {
        return new ResultModel<>(MessageCode.RESULT_OK);
    }

    public static ResultModel<Object> fail() {
        return new ResultModel<>(MessageCode.RESULT_FAIL);
    }

    public static ResultModel<Object> fail(MessageCode code) {
        return new ResultModel<>(code);
    }

    public static ResultModel<Object> fail(String message) {
        ResultModel<Object> result = fail();
        result.setMessage(message);
        return result;
    }

    public static ResultModel<Object> fail(String message, int code) {
        ResultModel<Object> result = new ResultModel<>();
        result.setMessage(message);
        result.setCode(code);
        return result;
    }
}
