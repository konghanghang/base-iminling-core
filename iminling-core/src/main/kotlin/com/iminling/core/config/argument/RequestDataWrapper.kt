package com.iminling.core.config.argument

import com.fasterxml.jackson.databind.JsonNode
import com.iminling.common.json.JsonUtil

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
class RequestDataWrapper {

    var params: JsonNode? = null
    var hasParams = false
    var canRead = false

    constructor(canRead: Boolean) {
        this.canRead = canRead
    }

    fun parseJsonNode(params: JsonNode?) {
        if (params != null) {
            this.params = params
            hasParams = !params.isNull && !params.isMissingNode
        }
    }

    override fun toString(): String {
        return "${JsonUtil.obj2Str(params)}"
    }

}