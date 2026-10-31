package com.iminling.core.log;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.util.LogUtils;
import com.iminling.core.util.ThreadContext;

import jakarta.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedHashMap;

@Getter
@Setter
public class RequestLog {
    private String type;
    private String uri, httpMethod, header, param, body, clientIp;

    public RequestLog() {
        this("spring");
    }

    public RequestLog(String type) {
        this.type = type;
    }

    public void wrap(HttpServletRequest request, String requestBody) {
        uri = request.getRequestURI();
        httpMethod = request.getMethod();
        param = JsonUtil.obj2Str(request.getParameterMap());
        body = requestBody;
        var headers = new LinkedHashMap<String, String>();
        if (request.getHeaderNames() != null)
            Collections.list(request.getHeaderNames())
                    .forEach(
                            name -> {
                                if (!LogUtils.containsHeader(name))
                                    headers.put(name, request.getHeader(name));
                            });
        header = JsonUtil.obj2Str(headers);
        clientIp =
                ThreadContext.getClientInfo() == null
                        ? null
                        : ThreadContext.getClientInfo().getRequestIp();
    }

    @Override
    public String toString() {
        return "Log_0 " + JsonUtil.obj2Str(this);
    }
}
