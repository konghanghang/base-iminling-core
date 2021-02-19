package com.iminling.model.exception;

import com.iminling.model.common.MessageCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户认证异常类
 * @author yslao@outlook.com
 */
public class AuthorizeException extends RuntimeException {

    @Getter @Setter private MessageCode messageCode;

    public AuthorizeException(){
        super(MessageCode.PERMISSION_DENY.getMessage());
        this.messageCode = MessageCode.PERMISSION_DENY;
    }

    public AuthorizeException(MessageCode messageCode){
        super(messageCode.getMessage());
        this.messageCode = messageCode;
    }

}
