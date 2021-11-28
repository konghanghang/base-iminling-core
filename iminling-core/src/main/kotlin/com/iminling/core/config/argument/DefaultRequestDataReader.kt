package com.iminling.core.config.argument

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.web.method.HandlerMethod
import java.io.IOException
import java.io.InputStream
import java.io.PushbackInputStream
import javax.validation.constraints.NotNull

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
class DefaultRequestDataReader(val objectMapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(DefaultRequestDataReader::class.java)

    private val suppotedMethod = setOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)

    fun canRead(message: @NotNull HttpInputMessage?): Boolean {
        val mediaType = message!!.headers.contentType
        if (!canRead(mediaType)) {
            return false
        }
        val httpMethod = if (message is HttpRequest) (message as HttpRequest).method else null
        return canRead(httpMethod)
    }

    private fun canRead(mediaType: @NotNull MediaType?): Boolean {
        if (mediaType == null) {
            return true
        }
        for (supportedMediaType in getSupportedMediaTypes()) {
            if (supportedMediaType.includes(mediaType)) {
                return true
            }
        }
        logger.info("Not support mediaType: {}", mediaType)
        return false
    }

    private fun canRead(httpMethod: @NotNull HttpMethod?): Boolean {
        if (httpMethod == null) {
            return true
        }
        if (suppotedMethod.contains(httpMethod)) {
            return true
        }
        logger.debug("Not support request method: {}", httpMethod)
        return false
    }

    @Throws(IOException::class)
    fun read(message: HttpInputMessage, handlerMethod: HandlerMethod?): JsonNode? {
        val inputStream = message.body
        val body: InputStream?
        if (inputStream.markSupported()) {
            inputStream.mark(1)
            body = if (inputStream.read() != -1) inputStream else null
        } else {
            val pushbackInputStream = PushbackInputStream(inputStream)
            val read = pushbackInputStream.read()
            if (read == -1) {
                body = null
            } else {
                body = pushbackInputStream
                pushbackInputStream.unread(read)
            }
        }
        return if (body != null) {
            objectMapper.readTree(body)
        } else null
    }

    fun getSupportedMediaTypes(): List<MediaType> {
        return listOf(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
    }

}