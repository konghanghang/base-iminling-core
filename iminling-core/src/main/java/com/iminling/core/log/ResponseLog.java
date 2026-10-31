package com.iminling.core.log;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.util.LogUtils;

import jakarta.servlet.http.*;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
public class ResponseLog {
    private String type;
    private String uri, httpMethod, message, header, error, requestBody, responseBody;
    private Integer respStatus;
    private int status;
    private long time;

    public ResponseLog() {
        this("spring");
    }

    public ResponseLog(String type) {
        this.type = type;
    }

    public void wrap(
            HttpServletRequest request,
            HttpServletResponse response,
            String requestBody,
            String responseBody,
            String error,
            long time) {
        uri = request.getRequestURI();
        httpMethod = request.getMethod();
        respStatus = response.getStatus();
        status = response.getStatus();
        this.time = time;
        message = "";
        var headers = new LinkedHashMap<String, String>();
        response.getHeaderNames()
                .forEach(
                        name -> {
                            if (!LogUtils.containsHeader(name))
                                headers.put(name, response.getHeader(name));
                        });
        header = JsonUtil.obj2Str(headers);
        this.error = error;
        this.requestBody = requestBody.substring(0, Math.min(500, requestBody.length()));
        this.responseBody = responseBody.substring(0, Math.min(500, responseBody.length()));
    }

    @Override
    public String toString() {
        return "Log_1 " + JsonUtil.obj2Str(this);
    }
}
