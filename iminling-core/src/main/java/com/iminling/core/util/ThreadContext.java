package com.iminling.core.util;

import com.iminling.core.log.ClientInfo;

import java.util.HashMap;
import java.util.Map;

public final class ThreadContext {
    private static final ThreadLocal<ClientInfo> CLIENT_INFO = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> PROPERTIES = new ThreadLocal<>();
    private static final String USER_KEY = "k0000-system-user-info";

    private ThreadContext() {}

    public static ClientInfo getClientInfo() {
        return CLIENT_INFO.get();
    }

    public static void setClientInfo(ClientInfo info) {
        CLIENT_INFO.set(info);
    }

    public static Object getAttribute(String key) {
        Map<String, Object> values = PROPERTIES.get();
        return values == null ? null : values.get(key);
    }

    public static void setAttribute(String key, Object value) {
        Map<String, Object> values = PROPERTIES.get();
        if (values == null) {
            values = new HashMap<>();
            PROPERTIES.set(values);
        }
        values.put(key, value);
    }

    public static <T> T getAccount(Class<T> type) {
        return type.cast(getAttribute(USER_KEY));
    }

    public static void setAccount(Object account) {
        setAttribute(USER_KEY, account);
    }

    public static void clear() {
        CLIENT_INFO.remove();
        PROPERTIES.remove();
    }
}
