package com.iminling.model.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClientInfo {

    private String token;

    private String requestIp;

    private String path;

    private String servletPath;

    private String contextPath;

}
