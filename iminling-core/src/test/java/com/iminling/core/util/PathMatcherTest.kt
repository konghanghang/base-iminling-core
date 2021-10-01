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
        var ignore = pathMatcher.ignore("xxxx.jpg")
        assert(ignore)
        ignore = pathMatcher.ignore("/myIndex")
        assert(!ignore)
        ignore = pathMatcher.ignore("/index")
        assert(ignore)
    }

}