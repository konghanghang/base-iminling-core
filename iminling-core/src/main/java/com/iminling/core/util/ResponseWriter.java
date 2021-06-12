package com.iminling.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iminling.model.common.ResultModel;
import com.iminling.model.exception.AuthorizeException;

import javax.servlet.http.HttpServletResponse;

public final class ResponseWriter {

    private ResponseWriter() {}

    public static void write(HttpServletResponse response, AuthorizeException ex) throws Exception {
        // HttpServletResponse.SC_FORBIDDEN
        response.setStatus(ex.getMessageCode().getCode());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin","*");
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(ResultModel.isFail(ex.getMessageCode())));
    }
}
