package com.iminling.core.util

import org.junit.jupiter.api.Test

/**
 *
 * @author yslao@outlook.com
 * @since 2021/10/1
 */
internal class PathMatcherTest {

    @Test
    fun urlTest() {
        var mutableListOf = mutableSetOf("/index")
        var pathMatcher = PathMatcher(mutableListOf)
        var match = pathMatcher.match("xxxx.jpg")
        assert(match)
        match = pathMatcher.match("/myIndex")
        assert(!match)
        match = pathMatcher.match("/index")
        assert(match)
    }

}