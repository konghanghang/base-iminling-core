package com.iminling.core.util

import org.springframework.util.AntPathMatcher

/**
 *
 * @author yslao@outlook.com
 * @since 2021/10/1
 */
class PathMatcher constructor(antUrl: MutableSet<String>) {

    private val resourceUrlSuffixSet = listOf("js", "gif", "jpg", "jpeg", "ico", "css", "ttd", "png").map { ".$it" }.toMutableSet()

    private var suffixUrl: MutableSet<String> = resourceUrlSuffixSet
    private var antUrl: MutableSet<String> = antUrl

    constructor(antUrl: MutableSet<String>, suffixUrl: MutableSet<String>): this(antUrl) {
        this.suffixUrl = suffixUrl
    }

    private val antPathMatcher = AntPathMatcher().apply {
        setCaseSensitive(false)
    }

    fun match(url: String): Boolean {
        return suffixUrl.any { url.endsWith(it) } || antUrl.any { antPathMatcher.match(it, url) }
    }

}