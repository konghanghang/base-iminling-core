package com.iminling.common.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

/**
 * 序列化string
 *
 * @author yslao@outlook.com
 * @since 2021/11/6
 */
class StringSerializer: JsonSerializer<String>() {
    override fun serialize(value: String?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeRawValue(value)
    }
}