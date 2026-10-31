package com.iminling.core.exception;

import com.iminling.core.constant.MessageCode;

public class BizException extends RuntimeException {
    private MessageCode messageCode;

    public BizException() {
        this(MessageCode.RESULT_FAIL);
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(MessageCode code) {
        super(code.getMessage());
        this.messageCode = code;
    }

    public MessageCode getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(MessageCode code) {
        this.messageCode = code;
    }
}
