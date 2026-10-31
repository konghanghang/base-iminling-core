package com.iminling.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Set;

class PathMatcherTest {
    @Test
    void matchesResourceSuffixAndAntPattern() {
        PathMatcher matcher = new PathMatcher(Set.of("/index", "/static/**"));
        assertTrue(matcher.match("xxxx.jpg"));
        assertFalse(matcher.match("/myIndex"));
        assertTrue(matcher.match("/index"));
        assertTrue(matcher.match("/STATIC/a/b"));
    }

    @Test
    void requestAttributesAreCleared() {
        ThreadContext.setAttribute("test", 1);
        ThreadContext.setAccount("account");
        assertEquals(1, ThreadContext.getAttribute("test"));
        assertEquals("account", ThreadContext.getAccount(String.class));
        ThreadContext.clear();
        assertNull(ThreadContext.getAttribute("test"));
        assertNull(ThreadContext.getAccount(String.class));
    }
}
