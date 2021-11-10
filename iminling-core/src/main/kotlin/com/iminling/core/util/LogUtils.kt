package com.iminling.core.util

import org.springframework.http.MediaType

/**
 *
 * @author yslao@outlook.com
 * @since 2021/10/1
 */
class LogUtils {

    companion object {
        private val IGNORE_URL_SET = mutableSetOf(
            "/error",
            "/do_not_delete/*", "/fonts/**", "/css/**", "/actuator/**", "/swagger/**", "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/springfox-swagger-ui/**"
        )
        private val IGNORE_HEADER = mutableSetOf(
                "User-Agent", "Cookie", "Accept", "Sec-Fetch-Dest", "Accept-Language",
                "Cache-Control", "Sec-Fetch-Mode", "Connection", "Accept-Encoding", "Upgrade-Insecure-Requests",
                "Sec-Fetch-Site", "Sec-Fetch-User"
            )
        private val HTTP_METHOD = mutableListOf("PUT", "POST", "DELETE", "GET")

        private val LOG_BODY_MEDIA_TYPE = listOf(
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_XML
        )

        private val urlMatcher: PathMatcher = PathMatcher(IGNORE_URL_SET)
        private val urlParamMatcher: PathMatcher = PathMatcher(IGNORE_URL_SET)
        private val urlResponseMatcher: PathMatcher = PathMatcher(IGNORE_URL_SET)

        fun canLog(url: String): Boolean {
            return !urlMatcher.ignore(url)
        }

        fun containsMethod(method: String): Boolean {
            return HTTP_METHOD.contains(method.uppercase())
        }

    }

}