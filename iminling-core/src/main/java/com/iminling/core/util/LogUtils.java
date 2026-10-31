package com.iminling.core.util;

import java.util.Locale;
import java.util.Set;

public final class LogUtils {
    private static final PathMatcher URL_MATCHER =
            new PathMatcher(
                    Set.of(
                            "/error",
                            "/do_not_delete/*",
                            "/fonts/**",
                            "/css/**",
                            "/actuator/**",
                            "/webjars/**"));
    private static final Set<String> IGNORE_HEADERS =
            Set.of(
                    "User-Agent",
                    "Cookie",
                    "Accept",
                    "Sec-Fetch-Dest",
                    "Accept-Language",
                    "Cache-Control",
                    "Sec-Fetch-Mode",
                    "Connection",
                    "Accept-Encoding",
                    "Upgrade-Insecure-Requests",
                    "Sec-Fetch-Site",
                    "Sec-Fetch-User");

    private LogUtils() {}

    public static boolean canLog(String url) {
        return !URL_MATCHER.match(url);
    }

    public static boolean containsMethod(String method) {
        return Set.of("PUT", "POST", "DELETE", "GET").contains(method.toUpperCase(Locale.ROOT));
    }

    public static boolean containsHeader(String header) {
        return IGNORE_HEADERS.stream().anyMatch(header::equalsIgnoreCase);
    }
}
