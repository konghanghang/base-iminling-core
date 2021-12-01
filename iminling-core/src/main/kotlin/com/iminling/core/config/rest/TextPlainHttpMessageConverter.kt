package com.iminling.core.config.rest

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.lang.Nullable
import java.nio.charset.StandardCharsets

/**
 * 处理text/plain类型
 * @author  yslao@outlook.com
 * @since  2021/12/1
 */
class TextPlainHttpMessageConverter(objectMapper: ObjectMapper):
    AbstractJackson2HttpMessageConverter(objectMapper, MediaType("text", "plain", StandardCharsets.UTF_8)) {

    @Nullable
    var jsonPrefix: String? = null

    fun setPrefixJson(prefixJson: Boolean) {
        jsonPrefix = if (prefixJson) ")]}', " else null
    }

    override fun writePrefix(generator: JsonGenerator, `object`: Any) {
        if (jsonPrefix != null) {
            generator.writeRaw(jsonPrefix)
        }
    }
}