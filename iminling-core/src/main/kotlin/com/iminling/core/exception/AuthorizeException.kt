package com.iminling.core.exception

import com.iminling.core.constant.MessageCode

/**
 * 用户认证异常类
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class AuthorizeException(open val messageCode: MessageCode): RuntimeException(messageCode.message) {

    constructor(): this(MessageCode.PERMISSION_DENY)

}