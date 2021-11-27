package com.iminling.core.exception

import com.iminling.core.constant.MessageCode

/**
 * 基础异常类
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class BizException(message: String): RuntimeException(message) {

    var messageCode: MessageCode? = null

    constructor(messageCode: MessageCode): this(messageCode.message) {
        this.messageCode = messageCode
    }

    constructor(): this(MessageCode.RESULT_FAIL)

}