package com.iminling.core.config.value

import com.iminling.core.constant.MessageCode

/**
 * 统一返回实体
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class ResultModel<T> {

    constructor()

    constructor(messageCode: MessageCode): this() {
        this.code = messageCode.code
        this.message = messageCode.message
    }

    /**
     * 状态码
     * @default 200
     */
    var code = 0

    /**
     * 返回的数据
     */
    var data: T? = null

    /**
     * 返回的消息
     */
    var message: String? = null

    companion object {
        fun ok(): ResultModel<Any> {
            return ResultModel(MessageCode.RESULT_OK)
        }

        fun fail(): ResultModel<Any> {
            return ResultModel(MessageCode.RESULT_FAIL)
        }

        fun fail(message: String): ResultModel<Any> {
            var resultModel = ResultModel<Any>(MessageCode.RESULT_FAIL)
            resultModel.message = message
            return resultModel
        }

        fun fail(messageCode: MessageCode): ResultModel<Any> {
            return ResultModel(messageCode)
        }

        fun fail(message: String, code: Int): ResultModel<Any> {
            var resultModel = ResultModel<Any>()
            resultModel.message = message
            resultModel.code = code
            return resultModel
        }
    }

}