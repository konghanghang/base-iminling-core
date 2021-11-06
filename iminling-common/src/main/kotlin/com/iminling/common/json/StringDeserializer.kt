package com.iminling.common.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

/**
 * 反序列化string
 *
 * @author yslao@outlook.com
 * @since 2021/11/6
 */
class StringDeserializer: JsonDeserializer<String>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): String {
        return p!!.text
    }
}