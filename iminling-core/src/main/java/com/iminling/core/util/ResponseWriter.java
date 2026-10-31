package com.iminling.core.util;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.config.value.ResultModel;
import com.iminling.core.exception.AuthorizeException;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class ResponseWriter {
    private ResponseWriter() {}

    public static void write(HttpServletResponse response, AuthorizeException error)
            throws IOException {
        response.setStatus(error.getMessageCode().getCode());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter()
                .write(
                        JsonUtil.getInstant()
                                .writeValueAsString(ResultModel.fail(error.getMessageCode())));
    }
}
