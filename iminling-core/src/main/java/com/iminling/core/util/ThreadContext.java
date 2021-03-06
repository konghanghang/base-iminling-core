package com.iminling.core.util;


import com.iminling.model.core.BaseUserInfo;
import com.iminling.model.core.ClientInfo;
import com.iminling.model.core.LogRecord;
import com.iminling.model.core.RequestInfo;

public final class ThreadContext {

    private ThreadContext() {}

    private final static ThreadLocal<BaseUserInfo> USER_INFO = new ThreadLocal<>();
    public static BaseUserInfo getUserInfo() {
        return USER_INFO.get();
    }
    public static void setUserInfo(BaseUserInfo baseUserInfo) {
        USER_INFO.set(baseUserInfo);
    }

    private final static ThreadLocal<ClientInfo> CLIENT_INFO = new ThreadLocal<>();
    public static ClientInfo getClientInfo() {
        return CLIENT_INFO.get();
    }
    public static void setClientInfo(ClientInfo clientInfo) {
        CLIENT_INFO.set(clientInfo);
    }

    private final static ThreadLocal<RequestInfo> REQUEST_INFO = new ThreadLocal<>();
    public static RequestInfo getRequestInfo() {
        return REQUEST_INFO.get();
    }
    public static void setRequestInfo(RequestInfo requestInfo) {
        REQUEST_INFO.set(requestInfo);
    }

    private final static ThreadLocal<LogRecord> LOG_RECORD = new ThreadLocal<>();
    public static LogRecord getLogRecord() {
        return LOG_RECORD.get();
    }
    public static void setLogRecord(LogRecord logRecord) {
        LOG_RECORD.set(logRecord);
    }

    public static void clear() {
        CLIENT_INFO.remove();
        USER_INFO.remove();
        REQUEST_INFO.remove();
        LOG_RECORD.remove();
    }
}
