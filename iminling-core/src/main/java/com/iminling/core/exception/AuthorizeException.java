package com.iminling.core.exception;

import com.iminling.core.constant.MessageCode;

public class AuthorizeException extends RuntimeException {
    private final MessageCode messageCode;

    public AuthorizeException() {
        this(MessageCode.PERMISSION_DENY);
    }

    public AuthorizeException(MessageCode code) {
        super(code.getMessage());
        this.messageCode = code;
    }

    public MessageCode getMessageCode() {
        return messageCode;
    }
}
