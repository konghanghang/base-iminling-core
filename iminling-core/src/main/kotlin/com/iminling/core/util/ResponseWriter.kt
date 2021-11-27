package com.iminling.core.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.iminling.core.config.value.ResultModel
import com.iminling.core.exception.AuthorizeException
import javax.servlet.http.HttpServletResponse

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class ResponseWriter {

    companion object {
        @Throws(Exception::class)
        fun write(response: HttpServletResponse, ex: AuthorizeException) {
            // HttpServletResponse.SC_FORBIDDEN
            response.status = ex.messageCode.code
            response.characterEncoding = "UTF-8"
            response.contentType = "application/json;charset=UTF-8"
            response.setHeader("Access-Control-Allow-Origin", "*")
            val objectMapper = ObjectMapper()
            response.writer.write(objectMapper.writeValueAsString(ResultModel.fail(ex.messageCode)))
        }
    }

}