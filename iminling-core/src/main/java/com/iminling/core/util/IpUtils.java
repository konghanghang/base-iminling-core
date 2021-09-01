package com.iminling.core.util;

import java.net.InetAddress;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpUtils {

    /**
     * 获取真实ip
     * @param request 请求
     * @return 真实ip
     */
    public static String getRemoteIpAddr(HttpServletRequest request) {
        String unknown = "unknown";
        String remoteIpAddr = unknown;
        try {
            String ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || unknown.equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || unknown.equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || unknown.equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)){
                    ipAddress = InetAddress.getLocalHost().getHostAddress();
                }
            } if (ipAddress != null && ipAddress.indexOf(',') > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(','));
            }
            remoteIpAddr = ipAddress;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return remoteIpAddr;
    }

}
