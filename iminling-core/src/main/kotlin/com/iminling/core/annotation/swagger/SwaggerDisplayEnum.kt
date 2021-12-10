package com.iminling.core.annotation.swagger

/**
 * @author  yslao@outlook.com
 * @since  2021/12/8
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SwaggerDisplayEnum(val code: String = "code", val desc: String = "desc")
