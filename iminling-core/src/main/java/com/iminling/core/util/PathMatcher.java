package com.iminling.core.util;

import org.springframework.util.AntPathMatcher;

import java.util.Set;

public class PathMatcher {
    private final Set<String> antUrl;
    private final Set<String> suffixUrl;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public PathMatcher(Set<String> antUrl) {
        this(antUrl, Set.of(".js", ".gif", ".jpg", ".jpeg", ".ico", ".css", ".ttd", ".png"));
    }

    public PathMatcher(Set<String> antUrl, Set<String> suffixUrl) {
        this.antUrl = antUrl;
        this.suffixUrl = suffixUrl;
        matcher.setCaseSensitive(false);
    }

    public boolean match(String url) {
        return suffixUrl.stream().anyMatch(url::endsWith)
                || antUrl.stream().anyMatch(pattern -> matcher.match(pattern, url));
    }
}
