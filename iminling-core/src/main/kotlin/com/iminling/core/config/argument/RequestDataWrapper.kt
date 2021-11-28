package com.iminling.core.config.argument

import com.fasterxml.jackson.databind.JsonNode

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

}